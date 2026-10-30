package io.github.dropio12.translatorai.domain

/** A point in pixels. */
data class Point(val x: Float, val y: Float)

/**
 * One block of text found on an image.
 *
 * [corners] are in pixels, in the order ML Kit gives them: top-left, top-right,
 * bottom-right, bottom-left. A tilted sign gives a tilted block.
 */
data class RecognizedBlock(val text: String, val corners: List<Point>) {
    init {
        require(corners.size == 4) { "A block needs 4 corners, got ${corners.size}" }
    }
}

/** A block and its translation. */
data class TranslatedBlock(val block: RecognizedBlock, val translation: String)

/**
 * What the overlay draws for one block: a patch of [background] color over the original
 * words, with [translation] written on it in [textColor]. Colors are ARGB ints.
 */
data class Patch(
    val box: OverlayBox,
    val original: String,
    val translation: String,
    val background: Int,
    val textColor: Int,
)
