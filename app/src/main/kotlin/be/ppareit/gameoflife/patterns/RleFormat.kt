package be.ppareit.gameoflife.patterns

object RleFormat : PatternFormat {
    private val headerRegex = Regex("""x\s*=\s*(\d+)\s*,\s*y\s*=\s*(\d+)(?:\s*,\s*rule\s*=\s*\S+)?\s*""")

    override fun canRead(text: String): Boolean {
        return text.lineSequence()
            .map { it.trim() }
            .firstOrNull { it.isNotEmpty() && !it.startsWith("#") }
            ?.startsWith("x") == true
    }

    override fun read(text: String): LifePattern {
        val lines = text.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.toList()
        val header = lines.firstOrNull { !it.startsWith("#") }
            ?: throw MalformedPatternFormatException()
        val match = headerRegex.matchEntire(header) ?: throw MalformedPatternFormatException()
        val width = match.groupValues[1].toIntOrNull() ?: throw MalformedPatternFormatException()
        val height = match.groupValues[2].toIntOrNull() ?: throw MalformedPatternFormatException()
        if (width <= 0 || height <= 0) {
            throw MalformedPatternFormatException()
        }

        val body = lines.dropWhile { it.startsWith("#") || it == header }
            .filterNot { it.startsWith("#") }
            .joinToString(separator = "")

        return LifePattern(readBody(body, width, height))
    }

    private fun readBody(body: String, width: Int, height: Int): Set<GridPoint> {
        val cells = mutableSetOf<GridPoint>()
        var row = 0
        var col = 0
        var runCount = 0
        var ended = false

        for (char in body) {
            if (ended) {
                if (!char.isWhitespace()) throw MalformedPatternFormatException()
                continue
            }

            when {
                char.isWhitespace() -> Unit
                char.isDigit() -> {
                    runCount = runCount * 10 + char.digitToInt()
                    if (runCount <= 0) throw MalformedPatternFormatException()
                }
                char == 'o' || char == 'b' -> {
                    val count = runCount.takeIf { it > 0 } ?: 1
                    if (row >= height || col + count > width) {
                        throw MalformedPatternFormatException()
                    }
                    if (char == 'o') {
                        repeat(count) { cells.add(GridPoint(col + it, row)) }
                    }
                    col += count
                    runCount = 0
                }
                char == '$' -> {
                    val count = runCount.takeIf { it > 0 } ?: 1
                    row += count
                    if (row > height) throw MalformedPatternFormatException()
                    col = 0
                    runCount = 0
                }
                char == '!' -> {
                    if (runCount != 0) throw MalformedPatternFormatException()
                    ended = true
                }
                else -> throw MalformedPatternFormatException()
            }
        }

        if (!ended) throw MalformedPatternFormatException()
        return cells
    }
}
