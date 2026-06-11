package be.ppareit.gameoflife

import be.ppareit.gameoflife.patterns.GridPoint
import be.ppareit.gameoflife.patterns.MalformedPatternFormatException
import be.ppareit.gameoflife.patterns.PatternFormatException
import be.ppareit.gameoflife.patterns.PatternFormats
import be.ppareit.gameoflife.patterns.RleFormat
import be.ppareit.gameoflife.patterns.UnsupportedPatternFormatException
import java.io.ByteArrayInputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class PatternFormatTest {
    @Test
    fun rleFormatReadsReplicatorCells() {
        val pattern = RleFormat.read(
            """
            #N Replicator
            #C A replicator for the HighLife rule.
            x = 5, y = 5, rule = 23/36
            2b3o${'$'}bo2bo${'$'}o3bo${'$'}o2bob${'$'}3o!
            """.trimIndent(),
        )

        assertEquals(
            setOf(
                GridPoint(2, 0),
                GridPoint(3, 0),
                GridPoint(4, 0),
                GridPoint(1, 1),
                GridPoint(4, 1),
                GridPoint(0, 2),
                GridPoint(4, 2),
                GridPoint(0, 3),
                GridPoint(3, 3),
                GridPoint(0, 4),
                GridPoint(1, 4),
                GridPoint(2, 4),
            ),
            pattern.cells,
        )
    }

    @Test
    fun rleFormatSupportsWhitespaceCommentsAndMultiDigitRuns() {
        val pattern = RleFormat.read(
            """
            #N Wide
            x = 12, y = 2
            10bo${'$'}
            #C comment between body lines
            12o!
            """.trimIndent(),
        )

        assertTrue(GridPoint(10, 0) in pattern.cells)
        assertEquals((0..11).map { GridPoint(it, 1) }.toSet() + GridPoint(10, 0), pattern.cells)
    }

    @Test
    fun rleFormatRejectsMalformedInput() {
        listOf(
            "2o!",
            "x = 2\n2o!",
            "x = 2, y = 1\n3o!",
            "x = 2, y = 1\n2q!",
            "x = 2, y = 1\n2o",
            "x = 2, y = 1\n2!",
        ).forEach { contents ->
            assertFormatNotSupported {
                RleFormat.read(contents)
            }
        }
    }

    @Test
    fun patternFormatsDistinguishesUnsupportedAndMalformedInput() {
        assertThrows<UnsupportedPatternFormatException> {
            PatternFormats.read("not a life pattern".asInputStream())
        }
        assertThrows<MalformedPatternFormatException> {
            PatternFormats.read("#Life 1.06\nbroken\n".asInputStream())
        }
        assertThrows<MalformedPatternFormatException> {
            PatternFormats.read("x = 2, y = 1\n3o!".asInputStream())
        }
    }

    private fun assertFormatNotSupported(block: () -> Unit) {
        try {
            block()
        } catch (_: PatternFormatException) {
            return
        }
        throw AssertionError("Expected PatternFormatException")
    }

    private inline fun <reified T : Throwable> assertThrows(block: () -> Unit) {
        try {
            block()
        } catch (e: Throwable) {
            if (e is T) return
            fail("Expected ${T::class.java.simpleName}, got ${e::class.java.simpleName}")
        }
        fail("Expected ${T::class.java.simpleName}")
    }

    private fun String.asInputStream(): ByteArrayInputStream = ByteArrayInputStream(toByteArray())
}
