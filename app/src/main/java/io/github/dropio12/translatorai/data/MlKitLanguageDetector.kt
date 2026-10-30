package io.github.dropio12.translatorai.data

import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentificationOptions
import io.github.dropio12.translatorai.domain.LanguageDetector
import kotlinx.coroutines.tasks.await

/** ML Kit language identification. The model is bundled in the app and runs on the phone. */
class MlKitLanguageDetector : LanguageDetector, AutoCloseable {

    private val client = LanguageIdentification.getClient(
        LanguageIdentificationOptions.Builder()
            .setConfidenceThreshold(CONFIDENCE_THRESHOLD)
            .build(),
    )

    override suspend fun detect(text: String): String = client.identifyLanguage(text).await()

    override fun close() = client.close()

    private companion object {
        const val CONFIDENCE_THRESHOLD = 0.5f
    }
}
