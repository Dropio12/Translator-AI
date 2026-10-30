package io.github.dropio12.translatorai.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TextFitterTest {

    // A fake monospace font: every character is half as wide as the font size.
    private val fitter = TextFitter(measureWidth = { text, size -> text.length * size * 0.5f }, lineSpacing = 1.2f)

    @Test
    fun `short text on a wide box uses one line as tall as the box allows`() {
        val fitted = fitter.fit("Exit", boxWidth = 400f, boxHeight = 60f)

        assertEquals(listOf("Exit"), fitted.lines)
        assertEquals(50f, fitted.fontSize, 0.01f) // 60 / 1.2
    }

    @Test
    fun `long text wraps on spaces and gets smaller`() {
        val fitted = fitter.fit("Roasted chicken with vegetables", boxWidth = 200f, boxHeight = 60f)

        assertTrue("expected several lines, got ${fitted.lines}", fitted.lines.size > 1)
        assertTrue(fitted.fontSize < 50f)
        assertEquals("Roasted chicken with vegetables", fitted.lines.joinToString(" "))
        assertFits(fitted, 200f, 60f)
    }

    @Test
    fun `picks the biggest size that fits`() {
        // At size 30 a line holds 10 characters: "Apple pie" / "of the day" is 2 lines, 72 tall.
        // Any bigger and "of the day" no longer fits, so it needs 3 lines, over 100 tall.
        val fitted = fitter.fit("Apple pie of the day", boxWidth = 150f, boxHeight = 80f)

        assertEquals(30f, fitted.fontSize, 0.5f)
        assertEquals(listOf("Apple pie", "of the day"), fitted.lines)
        assertFits(fitted, 150f, 80f)
    }

    @Test
    fun `words are kept whole when possible`() {
        val fitted = fitter.fit("Soup of the day", boxWidth = 120f, boxHeight = 200f)

        fitted.lines.forEach { line ->
            line.split(" ").forEach { word -> assertTrue(word in listOf("Soup", "of", "the", "day")) }
        }
    }

    @Test
    fun `text without spaces is broken between characters`() {
        // Chinese has no spaces between words, so it can break anywhere. 3 characters per line
        // at size 26.6 is bigger than the whole sentence on one line (size 11.4).
        val fitted = fitter.fit("今日的汤和烤鸡", boxWidth = 40f, boxHeight = 100f)

        assertEquals(listOf("今日的", "汤和烤", "鸡"), fitted.lines)
        assertEquals(26.6f, fitted.fontSize, 0.5f)
        assertFits(fitted, 40f, 100f)
    }

    @Test
    fun `a word too long for the box is broken only when nothing else works`() {
        val fitted = fitter.fit("Supercalifragilistic", boxWidth = 40f, boxHeight = 100f)

        assertTrue(fitted.lines.size > 1)
        assertEquals("Supercalifragilistic", fitted.lines.joinToString(""))
        assertFits(fitted, 40f, 100f)
    }

    @Test
    fun `text that cannot fit is cut and ends with an ellipsis`() {
        val text = "This is a very long translated sentence that will never fit in a tiny box"
        val fitted = fitter.fit(text, boxWidth = 30f, boxHeight = 10f, minSize = 6f)

        assertEquals(6f, fitted.fontSize, 0.01f)
        assertEquals(1, fitted.lines.size)
        assertTrue(fitted.lines.last().endsWith("…"))
        assertTrue(fitted.lines.last().length * 6f * 0.5f <= 30f)
    }

    @Test
    fun `blank text gives no lines`() {
        assertTrue(fitter.fit("   ", boxWidth = 100f, boxHeight = 50f).lines.isEmpty())
    }

    @Test
    fun `empty box gives no lines`() {
        assertTrue(fitter.fit("Menu", boxWidth = 0f, boxHeight = 50f).lines.isEmpty())
    }

    private fun assertFits(fitted: TextFitter.Fitted, boxWidth: Float, boxHeight: Float) {
        fitted.lines.forEach { line ->
            assertTrue("'$line' is too wide", line.length * fitted.fontSize * 0.5f <= boxWidth + 0.01f)
        }
        assertTrue("too many lines", fitted.lines.size * fitted.lineHeight <= boxHeight + 0.01f)
    }
}
