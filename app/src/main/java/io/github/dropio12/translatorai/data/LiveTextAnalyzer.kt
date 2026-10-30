package io.github.dropio12.translatorai.data

import androidx.camera.core.ImageAnalysis
import androidx.camera.mlkit.vision.MlKitAnalyzer
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import io.github.dropio12.translatorai.domain.RecognizedBlock
import java.util.concurrent.Executor

/**
 * Reads text from camera frames for live mode. CameraX moves the block corners into the
 * preview's own coordinates, so the overlay can be drawn right on top of the preview.
 */
class LiveTextAnalyzer(
    executor: Executor,
    onBlocks: (List<RecognizedBlock>) -> Unit,
) : AutoCloseable {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    val analyzer = MlKitAnalyzer(
        listOf(recognizer),
        ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED,
        executor,
    ) { result ->
        result.getValue(recognizer)?.let { text -> onBlocks(text.toBlocks()) }
    }

    override fun close() = recognizer.close()
}
