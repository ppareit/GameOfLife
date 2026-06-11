package be.ppareit.gameoflife.patterns

import java.io.OutputStream
import java.io.PrintWriter

object Life106Format : PatternFormat {
    override fun canRead(text: String): Boolean {
        return text.lineSequence().firstOrNull()?.trim() == "#Life 1.06"
    }

    override fun read(text: String): LifePattern {
        val cells = mutableSetOf<GridPoint>()
        val lines = text.lineSequence().iterator()
        if (!lines.hasNext() || lines.next().trim() != "#Life 1.06") {
            throw MalformedPatternFormatException()
        }

        while (lines.hasNext()) {
            val line = lines.next()
            if (line.isBlank()) continue
            val coords = line.trim().split(Regex("\\s+"))
            if (coords.size != 2) {
                throw MalformedPatternFormatException()
            }
            val x = coords[0].toIntOrNull() ?: throw MalformedPatternFormatException()
            val y = coords[1].toIntOrNull() ?: throw MalformedPatternFormatException()
            cells.add(GridPoint(x, y))
        }

        return LifePattern(cells)
    }

    fun write(cells: Set<GridPoint>, outputStream: OutputStream) {
        PrintWriter(outputStream).use { writer ->
            writer.println("#Life 1.06")
            for (cell in cells) {
                writer.println("${cell.x} ${cell.y}")
            }
        }
    }
}
