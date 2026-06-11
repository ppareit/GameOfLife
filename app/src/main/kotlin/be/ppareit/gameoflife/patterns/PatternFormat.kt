package be.ppareit.gameoflife.patterns

interface PatternFormat {
    fun canRead(text: String): Boolean
    fun read(text: String): LifePattern
}
