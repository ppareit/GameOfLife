package be.ppareit.gameoflife.patterns

import java.io.InputStream

object PatternFormats {
    private val formats = listOf(Life106Format, RleFormat)

    fun read(inputStream: InputStream): LifePattern {
        val text = inputStream.bufferedReader().use { it.readText() }
        val format = formats.firstOrNull { it.canRead(text) }
            ?: throw UnsupportedPatternFormatException()
        return format.read(text)
    }
}
