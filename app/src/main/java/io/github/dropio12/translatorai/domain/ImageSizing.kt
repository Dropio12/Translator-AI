package io.github.dropio12.translatorai.domain

import kotlin.math.roundToInt

object ImageSizing {

    /**
     * Scales (width, height) down so the longest side is at most [maxSide], keeping the shape.
     * Never scales up. Text recognition does not need a 12 megapixel photo.
     */
    fun fitWithin(width: Int, height: Int, maxSide: Int): Pair<Int, Int> {
        require(width > 0 && height > 0 && maxSide > 0) { "Sizes must be positive" }
        val longest = maxOf(width, height)
        if (longest <= maxSide) return width to height
        val scale = maxSide.toDouble() / longest
        return maxOf(1, (width * scale).roundToInt()) to maxOf(1, (height * scale).roundToInt())
    }
}
