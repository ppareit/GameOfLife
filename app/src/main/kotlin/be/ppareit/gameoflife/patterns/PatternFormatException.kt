package be.ppareit.gameoflife.patterns

sealed class PatternFormatException : RuntimeException()

class UnsupportedPatternFormatException : PatternFormatException()

class MalformedPatternFormatException : PatternFormatException()
