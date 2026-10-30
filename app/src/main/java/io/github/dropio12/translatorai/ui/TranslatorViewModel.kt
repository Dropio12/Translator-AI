package io.github.dropio12.translatorai.ui

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.dropio12.translatorai.data.ImageLoader
import io.github.dropio12.translatorai.data.MlKitLanguageDetector
import io.github.dropio12.translatorai.data.MlKitTextReader
import io.github.dropio12.translatorai.data.MlKitTranslator
import io.github.dropio12.translatorai.domain.CachingTranslator
import io.github.dropio12.translatorai.domain.LanguageCodes
import io.github.dropio12.translatorai.domain.LanguageDetector
import io.github.dropio12.translatorai.domain.OverlayGeometry
import io.github.dropio12.translatorai.domain.Patch
import io.github.dropio12.translatorai.domain.PatchColors
import io.github.dropio12.translatorai.domain.RecognizedBlock
import io.github.dropio12.translatorai.domain.TextCleanup
import io.github.dropio12.translatorai.domain.TextTranslator
import io.github.dropio12.translatorai.domain.TranslateBlocks
import io.github.dropio12.translatorai.domain.TranslatedBlock
import io.github.dropio12.translatorai.domain.TranslationStep
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class TranslatorViewModel(
    private val reader: MlKitTextReader,
    detector: LanguageDetector,
    translator: TextTranslator,
    private val images: ImageLoader,
    deviceLanguage: String,
) : ViewModel() {

    private val translateBlocks = TranslateBlocks(detector, translator)

    private val _state = MutableStateFlow(
        UiState(
            languages = LanguageCodes.sortedByName(translator.supportedLanguages),
            target = LanguageCodes.defaultTarget(deviceLanguage, translator.supportedLanguages),
        ),
    )
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var photoJob: Job? = null
    private var photoBlocks: List<RecognizedBlock> = emptyList()

    // Live mode: the newest frame's blocks. A StateFlow drops frames that arrive while the
    // previous one is still being translated, so the overlay never falls far behind.
    private val liveFrames = MutableStateFlow<List<RecognizedBlock>?>(null)

    init {
        viewModelScope.launch {
            liveFrames.filterNotNull().collect { translateLive(it) }
        }
    }

    // Photo mode

    fun onImagePicked(uri: Uri) = processPhoto { images.fromUri(uri) }

    fun onSampleRequested() = processPhoto { images.fromAsset(SAMPLE_ASSET) }

    /**
     * Called on a camera thread. The capture is converted and closed right there, before the
     * camera screen goes away, then the rest runs on the main thread.
     */
    fun onPhotoCaptured(image: ImageProxy) {
        val bitmap = try {
            image.use { images.fromCapture(it) }
        } catch (e: Exception) {
            Log.w(TAG, "Could not convert the capture", e)
            onPhotoFailed()
            return
        }
        viewModelScope.launch { processPhoto { bitmap } }
    }

    fun onPhotoFailed() = _state.update { it.copy(screen = Screen.Home, live = false, status = Status.Failed) }

    fun setShowOriginal(show: Boolean) = _state.update { it.copy(showOriginal = show) }

    fun setSource(code: String?) {
        _state.update { it.copy(source = code) }
        retranslatePhoto()
    }

    fun setTarget(code: String) {
        _state.update { it.copy(target = code) }
        retranslatePhoto()
    }

    private fun processPhoto(load: suspend () -> Bitmap) {
        photoJob?.cancel()
        photoBlocks = emptyList()
        liveFrames.value = null
        _state.update {
            it.copy(
                screen = Screen.Home,
                live = false,
                livePatches = emptyList(),
                image = null,
                patches = emptyList(),
                showOriginal = false,
                status = Status.Reading,
            )
        }
        photoJob = viewModelScope.launch {
            try {
                val bitmap = load()
                _state.update { it.copy(image = bitmap) }
                photoBlocks = reader.read(bitmap)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Could not read the image", e)
                _state.update { it.copy(status = Status.Failed) }
                return@launch
            }
            translatePhoto()
        }
    }

    /** After a failed download, tries the same translation again. */
    fun retry() = retranslatePhoto()

    /** The text is already read; only the languages changed. */
    private fun retranslatePhoto() {
        val current = _state.value
        if (current.image == null || current.status == Status.Reading) return
        photoJob?.cancel()
        photoJob = viewModelScope.launch {
            _state.update { it.copy(patches = emptyList()) }
            translatePhoto()
        }
    }

    private suspend fun translatePhoto() {
        val current = _state.value
        val bitmap = current.image ?: return
        val done = runTranslation(photoBlocks, current.target, current.source) { status ->
            _state.update { it.copy(status = status) }
        } ?: return
        val patches = withContext(Dispatchers.Default) {
            done.blocks.map { block ->
                val background = ImageLoader.backgroundAround(bitmap, block.block.corners)
                block.toPatch(background, PatchColors.textColorFor(background))
            }
        }
        _state.update {
            it.copy(patches = patches, status = Status.Done(done.source, done.detected, current.target))
        }
    }

    // Camera and live mode

    fun openCamera(live: Boolean) {
        liveFrames.value = null
        _state.update {
            it.copy(screen = Screen.Camera, live = live, livePatches = emptyList(), liveStatus = Status.Idle)
        }
    }

    fun closeCamera() {
        liveFrames.value = null
        _state.update { it.copy(screen = Screen.Home, live = false, livePatches = emptyList()) }
    }

    fun setLive(on: Boolean) {
        liveFrames.value = null
        _state.update { it.copy(live = on, livePatches = emptyList(), liveStatus = Status.Idle) }
    }

    fun onCameraDenied() = _state.update { it.copy(status = Status.CameraDenied) }

    /** Called by the camera analyzer for every frame it reads, on a background thread. */
    fun onLiveBlocks(blocks: List<RecognizedBlock>) {
        if (_state.value.live) liveFrames.value = blocks
    }

    private suspend fun translateLive(blocks: List<RecognizedBlock>) {
        val current = _state.value
        if (!current.live) return
        val done = runTranslation(blocks, current.target, current.source) { status ->
            // Detecting and translating take a few milliseconds per frame; showing them would flicker.
            if (status !is Status.Detecting && status !is Status.Translating) {
                _state.update { it.copy(liveStatus = status) }
            }
        }
        _state.update {
            if (!it.live) return@update it
            it.copy(
                livePatches = done?.blocks?.map { block ->
                    block.toPatch(PatchColors.LIVE_BACKGROUND, PatchColors.WHITE)
                }.orEmpty(),
                liveStatus = if (done != null) Status.Done(done.source, done.detected, current.target) else it.liveStatus,
            )
        }
    }

    // Shared

    /** Runs the steps of one translation, reports each one, and returns the result if there is one. */
    private suspend fun runTranslation(
        blocks: List<RecognizedBlock>,
        target: String,
        source: String?,
        report: (Status) -> Unit,
    ): TranslationStep.Done? {
        var downloading = false
        var done: TranslationStep.Done? = null
        try {
            translateBlocks(blocks, target, source).collect { step ->
                when (step) {
                    TranslationStep.DetectingLanguage -> report(Status.Detecting)
                    is TranslationStep.DownloadingModels -> {
                        downloading = true
                        report(Status.Downloading(step.source, step.target))
                    }
                    is TranslationStep.Translating -> report(Status.Translating(step.done, step.total))
                    TranslationStep.NoText -> report(Status.NoText)
                    TranslationStep.UnknownLanguage -> report(Status.UnknownLanguage)
                    is TranslationStep.Done -> done = step
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Translation failed", e)
            report(if (downloading) Status.DownloadFailed else Status.Failed)
            return null
        }
        return done
    }

    private fun TranslatedBlock.toPatch(background: Int, textColor: Int) = Patch(
        box = OverlayGeometry.patchBoxFor(block.corners),
        original = TextCleanup.joinLines(block.text),
        translation = translation,
        background = background,
        textColor = textColor,
    )

    companion object {
        private const val TAG = "TranslatorViewModel"
        private const val SAMPLE_ASSET = "sample-menu-fr.png"

        val Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val reader = MlKitTextReader()
                val detector = MlKitLanguageDetector()
                val translator = MlKitTranslator()
                TranslatorViewModel(
                    reader = reader,
                    detector = detector,
                    translator = CachingTranslator(translator),
                    images = ImageLoader(app),
                    deviceLanguage = Locale.getDefault().language,
                ).apply {
                    addCloseable(reader)
                    addCloseable(detector)
                    addCloseable(translator)
                }
            }
        }
    }
}
