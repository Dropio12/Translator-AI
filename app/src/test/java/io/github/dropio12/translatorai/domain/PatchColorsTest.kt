package io.github.dropio12.translatorai.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PatchColorsTest {

    @Test
    fun `ring points stay outside the box and inside the image`() {
        val points = PatchColors.ringPoints(left = 20, top = 30, right = 80, bottom = 50, imageWidth = 100, imageHeight = 100)

        assertTrue(points.isNotEmpty())
        points.forEach { (x, y) ->
            assertTrue(x in 0 until 100 && y in 0 until 100)
            val insideBox = x in 20..80 && y in 30..50
            assertFalse("($x, $y) is inside the box", insideBox)
        }
    }

    @Test
    fun `ring is clipped at the image edge`() {
        val points = PatchColors.ringPoints(left = 0, top = 0, right = 10, bottom = 10, imageWidth = 12, imageHeight = 12)

        points.forEach { (x, y) -> assertTrue(x in 0 until 12 && y in 0 until 12) }
    }

    @Test
    fun `no ring when the block fills the image`() {
        assertTrue(PatchColors.ringPoints(0, 0, 99, 99, imageWidth = 100, imageHeight = 100).isEmpty())
    }

    @Test
    fun `median ignores a few letter pixels`() {
        val cream = 0xFFF4EBD0.toInt()
        val pixels = IntArray(20) { if (it < 4) PatchColors.BLACK else cream }

        assertEquals(cream, PatchColors.median(pixels))
    }

    @Test
    fun `median of nothing is null`() {
        assertNull(PatchColors.median(IntArray(0)))
    }

    @Test
    fun `median result is opaque`() {
        val halfTransparentRed = 0x80FF0000.toInt()

        assertEquals(0xFFFF0000.toInt(), PatchColors.median(intArrayOf(halfTransparentRed)))
    }

    @Test
    fun `dark text on light patches, light text on dark patches`() {
        assertEquals(PatchColors.BLACK, PatchColors.textColorFor(0xFFFFFFFF.toInt()))
        assertEquals(PatchColors.BLACK, PatchColors.textColorFor(0xFFF2B705.toInt())) // yellow
        assertEquals(PatchColors.WHITE, PatchColors.textColorFor(0xFF1B2A4A.toInt())) // navy
        assertEquals(PatchColors.WHITE, PatchColors.textColorFor(0xFF2E7D32.toInt())) // dark green
    }

    @Test
    fun `luminance goes from black to white`() {
        assertEquals(0.0, PatchColors.luminance(PatchColors.BLACK), 1e-9)
        assertEquals(1.0, PatchColors.luminance(PatchColors.WHITE), 1e-9)
    }
}
