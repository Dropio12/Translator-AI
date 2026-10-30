package io.github.dropio12.translatorai.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LanguageCodesTest {

    private val supported = setOf("en", "fr", "es", "pt", "zh", "he", "id", "ja", "de")

    @Test
    fun `plain code is kept`() {
        assertEquals("fr", LanguageCodes.toTranslationCode("fr", supported))
    }

    @Test
    fun `region is dropped and case does not matter`() {
        assertEquals("pt", LanguageCodes.toTranslationCode("pt-BR", supported))
        assertEquals("zh", LanguageCodes.toTranslationCode("zh_CN", supported))
        assertEquals("es", LanguageCodes.toTranslationCode(" ES ", supported))
    }

    @Test
    fun `old codes are mapped to the new ones`() {
        assertEquals("he", LanguageCodes.toTranslationCode("iw", supported))
        assertEquals("id", LanguageCodes.toTranslationCode("in", supported))
    }

    @Test
    fun `undetermined, empty and unsupported give null`() {
        assertNull(LanguageCodes.toTranslationCode(LanguageCodes.UNDETERMINED, supported))
        assertNull(LanguageCodes.toTranslationCode("", supported))
        assertNull(LanguageCodes.toTranslationCode(null, supported))
        assertNull(LanguageCodes.toTranslationCode("tlh", supported))
    }

    @Test
    fun `romanized text cannot be translated`() {
        // Language identification returns "ja-Latn" for Japanese typed in Latin letters.
        assertNull(LanguageCodes.toTranslationCode("ja-Latn", supported))
    }

    @Test
    fun `default target follows the phone language when it is supported`() {
        assertEquals("fr", LanguageCodes.defaultTarget("fr", supported))
        assertEquals("he", LanguageCodes.defaultTarget("iw", supported))
        assertEquals("en", LanguageCodes.defaultTarget("sw", supported))
    }

    @Test
    fun `display names are readable`() {
        assertEquals("French", LanguageCodes.displayName("fr"))
        assertEquals("Chinese", LanguageCodes.displayName("zh"))
        assertEquals("qqq", LanguageCodes.displayName("qqq"))
    }

    @Test
    fun `menus are sorted by name, not by code`() {
        // de = German, es = Spanish, fr = French, ja = Japanese
        assertEquals(listOf("fr", "de", "ja", "es"), LanguageCodes.sortedByName(listOf("es", "de", "ja", "fr")))
    }
}
