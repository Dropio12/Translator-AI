package io.github.dropio12.translatorai.data

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import io.github.dropio12.translatorai.domain.TextTranslator
import kotlinx.coroutines.tasks.await
import java.util.concurrent.ConcurrentHashMap

/**
 * ML Kit on-device translation. Each language needs a model of about 30 MB, downloaded the
 * first time it is used. After that, translation works offline.
 */
class MlKitTranslator : TextTranslator, AutoCloseable {

    override val supportedLanguages: Set<String> = TranslateLanguage.getAllLanguages().toSet()

    private val modelManager = RemoteModelManager.getInstance()
    private val translators = ConcurrentHashMap<Pair<String, String>, Translator>()
    private val readyPairs = ConcurrentHashMap.newKeySet<Pair<String, String>>()

    override suspend fun isReady(source: String, target: String): Boolean {
        if (source to target in readyPairs) return true
        return listOf(source, target).all { language ->
            modelManager.isModelDownloaded(TranslateRemoteModel.Builder(language).build()).await()
        }
    }

    override suspend fun prepare(source: String, target: String) {
        if (source to target in readyPairs) return
        translatorFor(source, target).downloadModelIfNeeded(DownloadConditions.Builder().build()).await()
        readyPairs += source to target
    }

    override suspend fun translate(text: String, source: String, target: String): String =
        translatorFor(source, target).translate(text).await()

    private fun translatorFor(source: String, target: String): Translator =
        translators.getOrPut(source to target) {
            Translation.getClient(
                TranslatorOptions.Builder()
                    .setSourceLanguage(source)
                    .setTargetLanguage(target)
                    .build(),
            )
        }

    override fun close() {
        translators.values.forEach { it.close() }
        translators.clear()
    }
}
