package be.ppareit.gameoflife

import org.junit.Assert.assertEquals
import org.junit.Test

class BoardThemesTest {
    @Test
    fun findByIdResolvesStableIds() {
        assertEquals("light", BoardThemes.findById("light").id)
        assertEquals("dark", BoardThemes.findById("dark").id)
    }

    @Test
    fun findByIdFallsBackToDefaultTheme() {
        assertEquals(BoardThemes.default(), BoardThemes.findById(null))
        assertEquals(BoardThemes.default(), BoardThemes.findById("unknown"))
    }
}
