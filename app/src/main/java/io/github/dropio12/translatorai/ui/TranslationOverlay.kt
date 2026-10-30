package io.github.dropio12.translatorai.ui

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import io.github.dropio12.translatorai.domain.FitTransform
import io.github.dropio12.translatorai.domain.OverlayBox
import io.github.dropio12.translatorai.domain.Patch
import io.github.dropio12.translatorai.domain.TextFitter

/**
 * Draws each translation on a patch that covers the original words, in their place.
 *
 * When [imageWidth] and [imageHeight] are given, the patches are in image pixels and are
 * scaled the same way the image is (fit and centered). Without them, they are already in
 * this view's pixels, which is the case for the live camera preview.
 */
@Composable
fun TranslationOverlay(
    patches: List<Patch>,
    modifier: Modifier = Modifier,
    imageWidth: Int? = null,
    imageHeight: Int? = null,
) {
    val paint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
    }
    val fitter = remember(paint) {
        TextFitter(measureWidth = { text, fontSize ->
            paint.textSize = fontSize
            paint.measureText(text)
        })
    }

    Spacer(
        modifier.drawWithCache {
            val transform = if (imageWidth != null && imageHeight != null) {
                FitTransform.fit(imageWidth.toFloat(), imageHeight.toFloat(), size.width, size.height)
            } else {
                FitTransform.Identity
            }
            val laidOut = patches.map { patch ->
                val box = patch.box.mapTo(transform)
                val padding = minOf(box.width, box.height) * 0.08f
                LaidOutPatch(patch, box, fitter.fit(patch.translation, box.width - 2 * padding, box.height - 2 * padding))
            }
            onDrawBehind {
                laidOut.forEach { (patch, box, fitted) ->
                    rotate(box.angle, pivot = Offset(box.centerX, box.centerY)) {
                        drawRoundRect(
                            color = Color(patch.background),
                            topLeft = Offset(box.centerX - box.width / 2, box.centerY - box.height / 2),
                            size = Size(box.width, box.height),
                            cornerRadius = CornerRadius(minOf(box.width, box.height) * 0.12f),
                        )
                        drawIntoCanvas { canvas ->
                            paint.textSize = fitted.fontSize
                            paint.color = patch.textColor
                            // Center the lines in the box; each baseline sits in the middle of its line.
                            val top = box.centerY - fitted.lineHeight * fitted.lines.size / 2
                            val baselineShift = fitted.lineHeight / 2 - (paint.ascent() + paint.descent()) / 2
                            fitted.lines.forEachIndexed { index, line ->
                                canvas.nativeCanvas.drawText(
                                    line,
                                    box.centerX,
                                    top + index * fitted.lineHeight + baselineShift,
                                    paint,
                                )
                            }
                        }
                    }
                }
            }
        },
    )
}

private data class LaidOutPatch(val patch: Patch, val box: OverlayBox, val fitted: TextFitter.Fitted)
