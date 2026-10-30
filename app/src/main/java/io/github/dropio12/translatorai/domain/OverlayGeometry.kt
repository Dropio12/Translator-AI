package io.github.dropio12.translatorai.domain

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.min

/** A rectangle, maybe rotated, given by its center, size and angle in degrees (clockwise on screen). */
data class OverlayBox(
    val centerX: Float,
    val centerY: Float,
    val width: Float,
    val height: Float,
    val angle: Float,
) {
    /** The same box grown by [amount] on every side. */
    fun inflate(amount: Float) = copy(width = width + 2 * amount, height = height + 2 * amount)

    /** The same box after the image is scaled and moved by [transform]. */
    fun mapTo(transform: FitTransform) = OverlayBox(
        centerX = transform.offsetX + centerX * transform.scale,
        centerY = transform.offsetY + centerY * transform.scale,
        width = width * transform.scale,
        height = height * transform.scale,
        angle = angle,
    )
}

/** How an image is drawn inside a view: scaled by [scale], then moved by the offsets. */
data class FitTransform(val scale: Float, val offsetX: Float, val offsetY: Float) {
    companion object {
        val Identity = FitTransform(1f, 0f, 0f)

        /** The image is scaled to fit the view without cropping and centered, like ContentScale.Fit. */
        fun fit(imageWidth: Float, imageHeight: Float, viewWidth: Float, viewHeight: Float): FitTransform {
            if (imageWidth <= 0f || imageHeight <= 0f) return Identity
            val scale = min(viewWidth / imageWidth, viewHeight / imageHeight)
            return FitTransform(
                scale = scale,
                offsetX = (viewWidth - imageWidth * scale) / 2f,
                offsetY = (viewHeight - imageHeight * scale) / 2f,
            )
        }
    }
}

object OverlayGeometry {

    /** The 4 corners of an upright rectangle, in ML Kit order. */
    fun cornersOf(left: Float, top: Float, right: Float, bottom: Float): List<Point> = listOf(
        Point(left, top),
        Point(right, top),
        Point(right, bottom),
        Point(left, bottom),
    )

    /** Turns the 4 corners of a text block into the box the patch is drawn on. */
    fun boxFor(corners: List<Point>): OverlayBox {
        require(corners.size == 4) { "Expected 4 corners, got ${corners.size}" }
        val (topLeft, topRight, bottomRight, bottomLeft) = corners
        val width = (distance(topLeft, topRight) + distance(bottomLeft, bottomRight)) / 2f
        val height = (distance(topLeft, bottomLeft) + distance(topRight, bottomRight)) / 2f
        val radians = atan2(topRight.y - topLeft.y, topRight.x - topLeft.x)
        return OverlayBox(
            centerX = corners.map { it.x }.average().toFloat(),
            centerY = corners.map { it.y }.average().toFloat(),
            width = width,
            height = height,
            angle = radians * 180f / PI.toFloat(),
        )
    }

    /** Box for a patch: the text box grown a little, so the patch fully hides the original letters. */
    fun patchBoxFor(corners: List<Point>): OverlayBox {
        val box = boxFor(corners)
        return box.inflate(min(box.width, box.height) * 0.06f + 2f)
    }

    private fun distance(a: Point, b: Point) = hypot(b.x - a.x, b.y - a.y)
}
