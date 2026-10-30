package io.github.dropio12.translatorai.domain

import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/** Picks colors for a patch so it blends with the photo and its text stays readable. Colors are ARGB ints. */
object PatchColors {

    const val BLACK = 0xFF000000.toInt()
    const val WHITE = 0xFFFFFFFF.toInt()
    /** Used when there is no pixel around the block to sample, for example when it fills the image. */
    const val FALLBACK = 0xFFF5F5F5.toInt()
    /** Live mode has no access to the frame pixels, so it uses a fixed dark patch. */
    const val LIVE_BACKGROUND = 0xE6202020.toInt()

    /**
     * Pixel positions on a thin ring just outside a box, kept inside the image.
     * The ring is mostly background, so its color is the color the patch should have.
     */
    fun ringPoints(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        imageWidth: Int,
        imageHeight: Int,
        margin: Int = 3,
        spacing: Int = 4,
    ): List<Pair<Int, Int>> {
        val ringLeft = left - margin
        val ringTop = top - margin
        val ringRight = right + margin
        val ringBottom = bottom + margin
        val points = mutableListOf<Pair<Int, Int>>()
        for (x in ringLeft..ringRight step spacing) {
            points += x to ringTop
            points += x to ringBottom
        }
        for (y in ringTop..ringBottom step spacing) {
            points += ringLeft to y
            points += ringRight to y
        }
        return points
            .filter { (x, y) -> x in 0 until imageWidth && y in 0 until imageHeight }
            .distinct()
    }

    /**
     * The median of each channel. Unlike the average, a few dark letter pixels that touch
     * the ring do not change the result. Returns null for no pixels.
     */
    fun median(pixels: IntArray): Int? {
        if (pixels.isEmpty()) return null
        fun channel(shift: Int) = pixels.map { (it shr shift) and 0xFF }.sorted()[pixels.size / 2]
        return argb(channel(16), channel(8), channel(0))
    }

    /** Black or white, whichever has the higher contrast on [background]. */
    fun textColorFor(background: Int): Int =
        if (contrast(BLACK, background) >= contrast(WHITE, background)) BLACK else WHITE

    /** Relative luminance from the WCAG definition, 0 for black to 1 for white. */
    fun luminance(color: Int): Double {
        fun linear(channel: Int): Double {
            val c = channel / 255.0
            return if (c <= 0.03928) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * linear((color shr 16) and 0xFF) +
            0.7152 * linear((color shr 8) and 0xFF) +
            0.0722 * linear(color and 0xFF)
    }

    private fun contrast(a: Int, b: Int): Double {
        val la = luminance(a)
        val lb = luminance(b)
        return (max(la, lb) + 0.05) / (min(la, lb) + 0.05)
    }

    private fun argb(red: Int, green: Int, blue: Int): Int =
        (0xFF shl 24) or (red shl 16) or (green shl 8) or blue
}
