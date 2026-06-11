package be.ppareit.gameoflife

import be.ppareit.gameoflife.patterns.PatternFormatException
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import org.junit.Assert.assertEquals
import org.junit.Test

class GameOfLifeTest {
    @Test
    fun loadGridFromFileRejectsInvalidHeader() {
        val game = GameOfLife(5, 5)

        assertFormatNotSupported {
            game.loadGridFromFile("Life 1.06\n0 0\n".asInputStream())
        }
    }

    @Test
    fun loadGridFromFileRejectsMalformedCoordinates() {
        val game = GameOfLife(5, 5)

        listOf(
            "#Life 1.06\n0\n",
            "#Life 1.06\nx 0\n",
            "#Life 1.06\n0 y\n",
            "#Life 1.06\n0 0 0\n",
        ).forEach { contents ->
            assertFormatNotSupported {
                game.loadGridFromFile(contents.asInputStream())
            }
        }
    }

    @Test
    fun loadGridFromFileKeepsExistingGridWhenInputIsMalformed() {
        val game = GameOfLife(5, 5)
        game.loadGridFromFile("#Life 1.06\n0 0\n".asInputStream())

        assertFormatNotSupported {
            game.loadGridFromFile("#Life 1.06\nbroken\n".asInputStream())
        }

        assertEquals(1, game.grid[2][2])
    }

    @Test
    fun loadGridFromFileSupportsRle() {
        val game = GameOfLife(7, 7)

        game.loadGridFromFile(
            """
            #N Replicator
            x = 5, y = 5, rule = 23/36
            2b3o${'$'}bo2bo${'$'}o3bo${'$'}o2bob${'$'}3o!
            """.trimIndent().asInputStream(),
        )

        assertEquals(1, game.grid[1][3])
        assertEquals(1, game.grid[1][4])
        assertEquals(1, game.grid[1][5])
        assertEquals(1, game.grid[5][1])
        assertEquals(1, game.grid[5][2])
        assertEquals(1, game.grid[5][3])
    }

    @Test
    fun saveGridToFileStillWritesLife106() {
        val game = GameOfLife(5, 5)
        game.grid[2][1] = 1
        game.grid[3][2] = 1
        val output = ByteArrayOutputStream()

        game.saveGridToFile(output)

        assertEquals(
            """
            #Life 1.06
            0 0
            1 1

            """.trimIndent(),
            output.toString(),
        )
    }

    private fun assertFormatNotSupported(block: () -> Unit) {
        try {
            block()
        } catch (_: PatternFormatException) {
            return
        }
        throw AssertionError("Expected PatternFormatException")
    }

    private fun String.asInputStream(): ByteArrayInputStream = ByteArrayInputStream(toByteArray())
}
