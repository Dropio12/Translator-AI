package io.github.dropio12.translatorai.ui

import android.graphics.Bitmap
import io.github.dropio12.translatorai.domain.LanguageCodes
import io.github.dropio12.translatorai.domain.Patch

enum class Screen { Home, Camera }

/** What the user sees: the progress of a translation or why it stopped. */
sealed interface Status {
    data object Idle : Status
    data object Reading : Status
    data object Detecting : Status
    data class Downloading(val source: String, val target: String) : Status
    data class Translating(val done: Int, val total: Int) : Status
    data class Done(val source: String, val detected: Boolean, val target: String) : Status
    data object NoText : Status
    data object UnknownLanguage : Status
    data object DownloadFailed : Status
    data object Failed : Status
    data object CameraDenied : Status

    val isBusy: Boolean
        get() = this is Reading || this is Detecting || this is Downloading || this is Translating
}

data class UiState(
    val screen: Screen = Screen.Home,
    /** Every language the translator supports, sorted by name. */
    val languages: List<String> = emptyList(),
    /** Null means "detect the language". */
    val source: String? = null,
    val target: String = LanguageCodes.ENGLISH,
    val image: Bitmap? = null,
    val patches: List<Patch> = emptyList(),
    val status: Status = Status.Idle,
    val showOriginal: Boolean = false,
    val live: Boolean = false,
    /** In live mode the patches are in the camera preview's coordinates. */
    val livePatches: List<Patch> = emptyList(),
    val liveStatus: Status = Status.Idle,
)
