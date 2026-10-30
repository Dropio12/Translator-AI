package io.github.dropio12.translatorai.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.cos
import kotlin.math.sin

class OverlayGeometryTest {

    private val delta = 0.01f

    @Test
    fun `upright block gives a box with no rotation`() {
        val box = OverlayGeometry.boxFor(OverlayGeometry.cornersOf(10f, 20f, 110f, 60f))

        assertEquals(60f, box.centerX, delta)
        assertEquals(40f, box.centerY, delta)
        assertEquals(100f, box.width, delta)
        assertEquals(40f, box.height, delta)
        assertEquals(0f, box.angle, delta)
    }

    @Test
    fun `tilted block keeps its size and angle`() {
        // A 100 x 40 box turned 30 degrees clockwise around (200, 200).
        val corners = rotatedRectangle(200f, 200f, 100f, 40f, 30.0)

        val box = OverlayGeometry.boxFor(corners)

        assertEquals(200f, box.centerX, delta)
        assertEquals(200f, box.centerY, delta)
        assertEquals(100f, box.width, delta)
        assertEquals(40f, box.height, delta)
        assertEquals(30f, box.angle, delta)
    }

    @Test
    fun `text going up to the right has a negative angle`() {
        val box = OverlayGeometry.boxFor(rotatedRectangle(50f, 50f, 60f, 20f, -15.0))

        assertEquals(-15f, box.angle, delta)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `a block needs four corners`() {
        OverlayGeometry.boxFor(listOf(Point(0f, 0f), Point(1f, 0f), Point(1f, 1f)))
    }

    @Test
    fun `patch box is a little bigger than the text box`() {
        val corners = OverlayGeometry.cornersOf(0f, 0f, 200f, 50f)
        val text = OverlayGeometry.boxFor(corners)
        val patch = OverlayGeometry.patchBoxFor(corners)

        assertEquals(text.centerX, patch.centerX, delta)
        assertEquals(text.centerY, patch.centerY, delta)
        // Grows by 6% of the short side plus 2 px, on each side.
        assertEquals(200f + 2 * (50f * 0.06f + 2f), patch.width, delta)
        assertEquals(50f + 2 * (50f * 0.06f + 2f), patch.height, delta)
    }

    @Test
    fun `wide view adds space on the left and right`() {
        // A 1000 x 500 image in a 400 x 100 view is limited by the height: scale 0.2.
        val transform = FitTransform.fit(1000f, 500f, 400f, 100f)

        assertEquals(0.2f, transform.scale, delta)
        assertEquals(100f, transform.offsetX, delta)
        assertEquals(0f, transform.offsetY, delta)
    }

    @Test
    fun `tall view adds space above and below`() {
        val transform = FitTransform.fit(1000f, 500f, 500f, 1000f)

        assertEquals(0.5f, transform.scale, delta)
        assertEquals(0f, transform.offsetX, delta)
        assertEquals(375f, transform.offsetY, delta)
    }

    @Test
    fun `image boxes are moved into view coordinates`() {
        val transform = FitTransform.fit(1000f, 500f, 400f, 100f)
        val box = OverlayBox(centerX = 500f, centerY = 250f, width = 200f, height = 50f, angle = 12f)

        val mapped = box.mapTo(transform)

        assertEquals(200f, mapped.centerX, delta) // 100 + 500 * 0.2
        assertEquals(50f, mapped.centerY, delta)
        assertEquals(40f, mapped.width, delta)
        assertEquals(10f, mapped.height, delta)
        assertEquals(12f, mapped.angle, delta)
    }

    @Test
    fun `empty image gives the identity transform`() {
        assertEquals(FitTransform.Identity, FitTransform.fit(0f, 0f, 400f, 100f))
    }

    /** Corners in ML Kit order of a [width] x [height] rectangle turned [degrees] clockwise. */
    private fun rotatedRectangle(cx: Float, cy: Float, width: Float, height: Float, degrees: Double): List<Point> {
        val radians = Math.toRadians(degrees)
        fun turn(dx: Float, dy: Float) = Point(
            (cx + dx * cos(radians) - dy * sin(radians)).toFloat(),
            (cy + dx * sin(radians) + dy * cos(radians)).toFloat(),
        )
        val w = width / 2
        val h = height / 2
        return listOf(turn(-w, -h), turn(w, -h), turn(w, h), turn(-w, h))
    }
}
