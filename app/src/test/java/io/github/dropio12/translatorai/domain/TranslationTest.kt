package io.github.dropio12.translatorai.domain

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TranslationTest {

    private class FakeDetector(private val tag: String) : LanguageDetector {
        val seen = mutableListOf<String>()
        override suspend fun detect(text: String): String {
            seen += text
            return tag
        }
    }

    private class FakeTranslator(var ready: Boolean = true) : TextTranslator {
        override val supportedLanguages = setOf("en", "fr", "es")
        val prepared = mutableListOf<Pair<String, String>>()
        val translated = mutableListOf<String>()

        override suspend fun isReady(source: String, target: String) = ready
        override suspend fun prepare(source: String, target: String) {
            prepared += source to target
            ready = true
        }
        override suspend fun translate(text: String, source: String, target: String): String {
            translated += text
            return "[$source>$target] $text"
        }
    }

    private fun block(text: String) = RecognizedBlock(text, OverlayGeometry.cornersOf(0f, 0f, 10f, 10f))

    @Test
    fun `no blocks means no text`() = runTest {
        val steps = TranslateBlocks(FakeDetector("fr"), FakeTranslator())(listOf(block("  ")), target = "en").toList()

        assertEquals(listOf(TranslationStep.NoText), steps)
    }

    @Test
    fun `detects the language on all the text, then translates each block`() = runTest {
        val detector = FakeDetector("fr")
        val translator = FakeTranslator()

        val steps = TranslateBlocks(detector, translator)(listOf(block("Sortie"), block("Menu du\njour")), target = "en").toList()

        assertEquals(listOf("Sortie Menu du jour"), detector.seen)
        assertEquals(
            listOf(
                TranslationStep.DetectingLanguage,
                TranslationStep.Translating(1, 2),
                TranslationStep.Translating(2, 2),
            ),
            steps.dropLast(1),
        )
        val done = steps.last() as TranslationStep.Done
        assertEquals("fr", done.source)
        assertTrue(done.detected)
        assertEquals(listOf("[fr>en] Sortie", "[fr>en] Menu du jour"), done.blocks.map { it.translation })
    }

    @Test
    fun `download step only shows when the models are missing`() = runTest {
        val translator = FakeTranslator(ready = false)

        val steps = TranslateBlocks(FakeDetector("es"), translator)(listOf(block("Hola")), target = "en").toList()

        assertTrue(TranslationStep.DownloadingModels("es", "en") in steps)
        assertEquals(listOf("es" to "en"), translator.prepared)

        val again = TranslateBlocks(FakeDetector("es"), translator)(listOf(block("Hola")), target = "en").toList()
        assertFalse(again.any { it is TranslationStep.DownloadingModels })
    }

    @Test
    fun `chosen source language skips detection`() = runTest {
        val detector = FakeDetector("es")

        val steps = TranslateBlocks(detector, FakeTranslator())(listOf(block("Bonjour")), target = "en", source = "fr").toList()

        assertTrue(detector.seen.isEmpty())
        assertFalse(TranslationStep.DetectingLanguage in steps)
        val done = steps.last() as TranslationStep.Done
        assertEquals("fr", done.source)
        assertFalse(done.detected)
    }

    @Test
    fun `unknown or unsupported language stops`() = runTest {
        val undetermined = TranslateBlocks(FakeDetector(LanguageCodes.UNDETERMINED), FakeTranslator())(listOf(block("xyz")), "en").toList()
        val unsupported = TranslateBlocks(FakeDetector("tlh"), FakeTranslator())(listOf(block("Qapla")), "en").toList()

        assertEquals(TranslationStep.UnknownLanguage, undetermined.last())
        assertEquals(TranslationStep.UnknownLanguage, unsupported.last())
    }

    @Test
    fun `same language is not sent to the translator`() = runTest {
        val translator = FakeTranslator()

        val steps = TranslateBlocks(FakeDetector("en"), translator)(listOf(block("Exit")), target = "en").toList()

        assertTrue(translator.translated.isEmpty())
        assertTrue(translator.prepared.isEmpty())
        assertEquals("Exit", (steps.last() as TranslationStep.Done).blocks.single().translation)
    }

    @Test
    fun `lines are joined and hyphenated words mended`() {
        assertEquals("Poulet rôti et légumes", TextCleanup.joinLines("Poulet rôti\n  et légumes "))
        assertEquals("traduction du jour", TextCleanup.joinLines("tra-\nduction du jour"))
        assertEquals("Saint-Tropez", TextCleanup.joinLines("Saint-\nTropez"))
        assertEquals("Prix - 10 euros", TextCleanup.joinLines("Prix -\n10 euros"))
        assertEquals("", TextCleanup.joinLines("\n \n"))
    }

    @Test
    fun `cache translates each text once`() = runTest {
        val inner = FakeTranslator()
        val cached = CachingTranslator(inner)

        repeat(3) { cached.translate("Sortie", "fr", "en") }
        cached.translate("Sortie", "fr", "es")

        assertEquals(listOf("Sortie", "Sortie"), inner.translated)
    }

    @Test
    fun `cache forgets the oldest entries`() = runTest {
        val inner = FakeTranslator()
        val cached = CachingTranslator(inner, maxEntries = 2)

        cached.translate("a", "fr", "en")
        cached.translate("b", "fr", "en")
        cached.translate("c", "fr", "en")
        cached.translate("a", "fr", "en")

        assertEquals(listOf("a", "b", "c", "a"), inner.translated)
    }
}
