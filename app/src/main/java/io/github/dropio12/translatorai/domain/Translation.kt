package io.github.dropio12.translatorai.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** Tells which language a text is written in. */
interface LanguageDetector {
    /** A BCP-47 tag such as "fr", or [LanguageCodes.UNDETERMINED] when it cannot tell. */
    suspend fun detect(text: String): String
}

/** Translates text between two languages, both given as translation codes. */
interface TextTranslator {
    val supportedLanguages: Set<String>

    /** True when the models for both languages are already on the phone. */
    suspend fun isReady(source: String, target: String): Boolean

    /** Downloads the models if they are missing. */
    suspend fun prepare(source: String, target: String)

    suspend fun translate(text: String, source: String, target: String): String
}

/** The steps of one translation, in the order they happen. */
sealed interface TranslationStep {
    data object DetectingLanguage : TranslationStep
    data class DownloadingModels(val source: String, val target: String) : TranslationStep
    data class Translating(val done: Int, val total: Int) : TranslationStep
    data class Done(val source: String, val detected: Boolean, val blocks: List<TranslatedBlock>) : TranslationStep
    data object NoText : TranslationStep
    data object UnknownLanguage : TranslationStep
}

/**
 * Detects the language of the recognized blocks, then translates each block.
 * The language is detected on all the text at once: a single block like "Sortie" is often
 * too short to tell, but the whole sign usually is not.
 */
class TranslateBlocks(
    private val detector: LanguageDetector,
    private val translator: TextTranslator,
) {
    /** Pass [source] to skip detection, or null to detect the language. */
    operator fun invoke(
        blocks: List<RecognizedBlock>,
        target: String,
        source: String? = null,
    ): Flow<TranslationStep> = flow {
        val usable = blocks.filter { it.text.isNotBlank() }
        if (usable.isEmpty()) {
            emit(TranslationStep.NoText)
            return@flow
        }

        val language = source ?: run {
            emit(TranslationStep.DetectingLanguage)
            val tag = detector.detect(usable.joinToString(" ") { TextCleanup.joinLines(it.text) })
            LanguageCodes.toTranslationCode(tag, translator.supportedLanguages)
        }
        if (language == null) {
            emit(TranslationStep.UnknownLanguage)
            return@flow
        }

        val detected = source == null
        if (language == target) {
            emit(TranslationStep.Done(language, detected, usable.map { TranslatedBlock(it, TextCleanup.joinLines(it.text)) }))
            return@flow
        }

        if (!translator.isReady(language, target)) {
            emit(TranslationStep.DownloadingModels(language, target))
        }
        translator.prepare(language, target)

        val translated = usable.mapIndexed { index, block ->
            emit(TranslationStep.Translating(index + 1, usable.size))
            TranslatedBlock(block, translator.translate(TextCleanup.joinLines(block.text), language, target))
        }
        emit(TranslationStep.Done(language, detected, translated))
    }
}

object TextCleanup {

    /**
     * ML Kit returns a block as several lines. Joins them into one sentence for translation.
     * A line that ends with a hyphen right after a letter continues on the next line:
     * "tra-" + "duction" is one word cut in two, "Saint-" + "Tropez" is a hyphenated name.
     */
    fun joinLines(text: String): String {
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) return ""
        return lines.drop(1).fold(lines.first()) { joined, next ->
            val continues = joined.length > 1 && joined.endsWith('-') && joined[joined.length - 2].isLetter()
            when {
                continues && next.first().isLowerCase() -> joined.dropLast(1) + next
                continues -> joined + next
                else -> "$joined $next"
            }
        }
    }
}

/**
 * Remembers recent translations. Live mode sees the same words in frame after frame,
 * so most of them are translated only once.
 */
class CachingTranslator(
    private val inner: TextTranslator,
    private val maxEntries: Int = 500,
) : TextTranslator by inner {

    private val cache = object : LinkedHashMap<String, String>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, String>?) = size > maxEntries
    }

    override suspend fun translate(text: String, source: String, target: String): String {
        val key = "$source|$target|$text"
        synchronized(cache) { cache[key] }?.let { return it }
        val translation = inner.translate(text, source, target)
        synchronized(cache) { cache[key] = translation }
        return translation
    }
}
