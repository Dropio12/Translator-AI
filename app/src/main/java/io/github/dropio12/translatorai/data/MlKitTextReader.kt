package io.github.dropio12.translatorai.data

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import io.github.dropio12.translatorai.domain.OverlayGeometry
import io.github.dropio12.translatorai.domain.Point
import io.github.dropio12.translatorai.domain.RecognizedBlock
import kotlinx.coroutines.tasks.await

/** Reads the text on a photo with ML Kit text recognition (Latin script model, bundled in the app). */
class MlKitTextReader : AutoCloseable {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun read(bitmap: Bitmap): List<RecognizedBlock> =
        recognizer.process(InputImage.fromBitmap(bitmap, 0)).await().toBlocks()

    override fun close() = recognizer.close()
}

/** Keeps the text and the corners of each block. Blocks with no position are dropped. */
internal fun Text.toBlocks(): List<RecognizedBlock> = textBlocks.mapNotNull { block ->
    val corners = block.cornerPoints
        ?.takeIf { it.size == 4 }
        ?.map { Point(it.x.toFloat(), it.y.toFloat()) }
        ?: block.boundingBox?.let {
            OverlayGeometry.cornersOf(it.left.toFloat(), it.top.toFloat(), it.right.toFloat(), it.bottom.toFloat())
        }
    if (corners == null || block.text.isBlank()) null else RecognizedBlock(block.text, corners)
}
