package io.github.dropio12.translatorai.domain

/**
 * Finds the biggest font size at which a text fits in a box, and where to break the lines.
 *
 * [measureWidth] gives the width of a piece of text at a font size, in the same unit as the
 * box. On the phone it is backed by android.graphics.Paint, in the tests by a fake.
 */
class TextFitter(
    private val measureWidth: (text: String, fontSize: Float) -> Float,
    private val lineSpacing: Float = 1.15f,
) {

    data class Fitted(val fontSize: Float, val lineHeight: Float, val lines: List<String>)

    fun fit(
        text: String,
        boxWidth: Float,
        boxHeight: Float,
        minSize: Float = 6f,
        maxSize: Float = 200f,
    ): Fitted {
        val words = text.split(WHITESPACE).filter { it.isNotEmpty() }
        if (words.isEmpty() || boxWidth <= 0f || boxHeight <= 0f) {
            return Fitted(minSize, minSize * lineSpacing, emptyList())
        }
        // A single line can never be taller than the box.
        val biggest = minOf(maxSize, boxHeight / lineSpacing)
        // Keep words whole if possible. Text written without spaces, like Chinese or
        // Japanese, can break between any two characters.
        val passes = if (text.any { it.isWrittenWithoutSpaces() }) listOf(true) else listOf(false, true)
        for (breakWords in passes) {
            largestFitting(words, boxWidth, boxHeight, minSize, biggest, breakWords)?.let { return it }
        }
        return truncated(words, boxWidth, boxHeight, minSize)
    }

    /** Binary search on the font size. Bigger text never needs fewer lines, so this is safe. */
    private fun largestFitting(
        words: List<String>,
        boxWidth: Float,
        boxHeight: Float,
        minSize: Float,
        maxSize: Float,
        breakWords: Boolean,
    ): Fitted? {
        if (maxSize < minSize) return null
        fun layout(size: Float): Fitted? {
            val lines = wrap(words, boxWidth, size, breakWords) ?: return null
            val lineHeight = size * lineSpacing
            return if (lines.size * lineHeight <= boxHeight + EPSILON) Fitted(size, lineHeight, lines) else null
        }
        layout(maxSize)?.let { return it }
        var best = layout(minSize) ?: return null
        var low = minSize
        var high = maxSize
        while (high - low > PRECISION) {
            val middle = (low + high) / 2f
            val fitted = layout(middle)
            if (fitted != null) {
                best = fitted
                low = middle
            } else {
                high = middle
            }
        }
        return best
    }

    /** Greedy word wrap. Returns null if a word is wider than the box and [breakWords] is false. */
    private fun wrap(words: List<String>, maxWidth: Float, size: Float, breakWords: Boolean): List<String>? {
        val lines = mutableListOf<String>()
        var current = ""
        for (word in words) {
            val pieces = when {
                measureWidth(word, size) <= maxWidth -> listOf(word)
                breakWords -> splitToWidth(word, maxWidth, size)
                else -> return null
            }
            for (piece in pieces) {
                val candidate = if (current.isEmpty()) piece else "$current $piece"
                if (measureWidth(candidate, size) <= maxWidth) {
                    current = candidate
                } else {
                    if (current.isNotEmpty()) lines += current
                    current = piece
                }
            }
        }
        if (current.isNotEmpty()) lines += current
        return lines
    }

    /** Cuts a word into pieces no wider than [maxWidth], with at least one character each. */
    private fun splitToWidth(word: String, maxWidth: Float, size: Float): List<String> {
        val pieces = mutableListOf<String>()
        val piece = StringBuilder()
        for (char in word) {
            if (piece.isNotEmpty() && measureWidth("$piece$char", size) > maxWidth) {
                pieces += piece.toString()
                piece.clear()
            }
            piece.append(char)
        }
        if (piece.isNotEmpty()) pieces += piece.toString()
        return pieces
    }

    /** Last resort: smallest size, as many lines as fit, and "…" at the end of the last one. */
    private fun truncated(words: List<String>, boxWidth: Float, boxHeight: Float, minSize: Float): Fitted {
        val lineHeight = minSize * lineSpacing
        val maxLines = maxOf(1, (boxHeight / lineHeight).toInt())
        val lines = wrap(words, boxWidth, minSize, breakWords = true).orEmpty().take(maxLines).toMutableList()
        if (lines.isNotEmpty()) {
            var last = lines.last()
            while (last.isNotEmpty() && measureWidth("$last$ELLIPSIS", minSize) > boxWidth) {
                last = last.dropLast(1)
            }
            lines[lines.lastIndex] = last.trimEnd() + ELLIPSIS
        }
        return Fitted(minSize, lineHeight, lines)
    }

    private fun Char.isWrittenWithoutSpaces(): Boolean = when (Character.UnicodeScript.of(code)) {
        Character.UnicodeScript.HAN,
        Character.UnicodeScript.HIRAGANA,
        Character.UnicodeScript.KATAKANA,
        Character.UnicodeScript.THAI,
        Character.UnicodeScript.LAO,
        Character.UnicodeScript.KHMER,
        Character.UnicodeScript.MYANMAR,
        -> true
        else -> false
    }

    private companion object {
        val WHITESPACE = Regex("\\s+")
        const val PRECISION = 0.5f
        const val EPSILON = 0.01f
        const val ELLIPSIS = "…"
    }
}
