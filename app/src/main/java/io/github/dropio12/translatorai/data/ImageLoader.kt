package io.github.dropio12.translatorai.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import androidx.camera.core.ImageProxy
import io.github.dropio12.translatorai.domain.ImageSizing
import io.github.dropio12.translatorai.domain.PatchColors
import io.github.dropio12.translatorai.domain.Point
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Turns gallery pictures, the bundled sample and camera captures into upright, reasonably sized bitmaps. */
class ImageLoader(private val context: Context) {

    suspend fun fromUri(uri: Uri): Bitmap = withContext(Dispatchers.IO) {
        decode(ImageDecoder.createSource(context.contentResolver, uri))
    }

    suspend fun fromAsset(name: String): Bitmap = withContext(Dispatchers.IO) {
        decode(ImageDecoder.createSource(context.assets, name))
    }

    /** A camera capture comes in the sensor's orientation; rotate it upright and scale it down. */
    fun fromCapture(image: ImageProxy): Bitmap {
        val raw = image.toBitmap()
        val (width, height) = ImageSizing.fitWithin(raw.width, raw.height, MAX_SIDE)
        val matrix = Matrix().apply {
            postScale(width.toFloat() / raw.width, height.toFloat() / raw.height)
            postRotate(image.imageInfo.rotationDegrees.toFloat())
        }
        return Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, matrix, true)
    }

    // ImageDecoder applies the EXIF rotation. A software bitmap is needed to read its pixels.
    private fun decode(source: ImageDecoder.Source): Bitmap =
        ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            val (width, height) = ImageSizing.fitWithin(info.size.width, info.size.height, MAX_SIDE)
            decoder.setTargetSize(width, height)
        }

    companion object {
        const val MAX_SIDE = 2048

        /** The color just around a text block, used to paint its patch. */
        fun backgroundAround(bitmap: Bitmap, corners: List<Point>): Int {
            val points = PatchColors.ringPoints(
                left = corners.minOf { it.x }.toInt(),
                top = corners.minOf { it.y }.toInt(),
                right = corners.maxOf { it.x }.toInt(),
                bottom = corners.maxOf { it.y }.toInt(),
                imageWidth = bitmap.width,
                imageHeight = bitmap.height,
            )
            val pixels = IntArray(points.size) { i -> bitmap.getPixel(points[i].first, points[i].second) }
            return PatchColors.median(pixels) ?: PatchColors.FALLBACK
        }
    }
}
