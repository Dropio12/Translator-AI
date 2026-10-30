package io.github.dropio12.translatorai.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ImageSizingTest {

    @Test
    fun `small images are not scaled up`() {
        assertEquals(800 to 600, ImageSizing.fitWithin(800, 600, maxSide = 2048))
    }

    @Test
    fun `big photos are scaled down keeping their shape`() {
        assertEquals(2048 to 1536, ImageSizing.fitWithin(4000, 3000, maxSide = 2048))
        assertEquals(1536 to 2048, ImageSizing.fitWithin(3000, 4000, maxSide = 2048))
    }

    @Test
    fun `very thin images keep at least one pixel`() {
        assertEquals(2048 to 1, ImageSizing.fitWithin(100_000, 1, maxSide = 2048))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `sizes must be positive`() {
        ImageSizing.fitWithin(0, 100, maxSide = 2048)
    }
}
