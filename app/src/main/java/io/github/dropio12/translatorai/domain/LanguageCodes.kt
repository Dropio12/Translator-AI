package io.github.dropio12.translatorai.domain

import java.util.Locale

/** Converts between the language tags of ML Kit language identification and translation. */
object LanguageCodes {

    /** What language identification returns when it cannot tell. */
    const val UNDETERMINED = "und"
    const val ENGLISH = "en"

    /** Old ISO codes that some systems still return. Translation uses the new ones. */
    private val legacyCodes = mapOf("iw" to "he", "in" to "id", "ji" to "yi")

    /**
     * Turns a tag such as "fr", "pt-BR" or "iw" into a code the translator supports,
     * or null when the text cannot be translated.
     */
    fun toTranslationCode(tag: String?, supported: Set<String>): String? {
        val parts = tag.orEmpty().trim().replace('_', '-').lowercase(Locale.ROOT)
            .split('-').filter { it.isNotEmpty() }
        val language = parts.firstOrNull() ?: return null
        if (language == UNDETERMINED) return null
        // Language identification tags romanized text with the Latin script, for example
        // "hi-Latn" for Hindi typed in Latin letters. The translation models only read the
        // native script, so that text cannot be translated.
        if ("latn" in parts.drop(1)) return null
        val code = legacyCodes[language] ?: language
        return code.takeIf { it in supported }
    }

    /** The target language to start with: the phone's language if supported, else English. */
    fun defaultTarget(deviceLanguage: String, supported: Set<String>): String =
        toTranslationCode(deviceLanguage, supported) ?: ENGLISH

    /** A readable name, like "French" for "fr". Falls back to the code itself. */
    fun displayName(code: String, inLocale: Locale = Locale.ENGLISH): String {
        val name = Locale.forLanguageTag(code).getDisplayLanguage(inLocale)
        return if (name.isBlank() || name.equals(code, ignoreCase = true)) {
            code
        } else {
            name.replaceFirstChar { it.titlecase(inLocale) }
        }
    }

    /** Codes sorted by their readable name, for the language menus. */
    fun sortedByName(codes: Collection<String>, inLocale: Locale = Locale.ENGLISH): List<String> =
        codes.sortedBy { displayName(it, inLocale) }
}
