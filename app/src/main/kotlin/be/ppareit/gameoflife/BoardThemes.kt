package be.ppareit.gameoflife

data class BoardThemeSpec(
    val id: String,
    val labelRes: Int,
    val backgroundColorRes: Int,
    val deadCellDrawableRes: Int,
    val liveCellDrawableRes: Int,
)

object BoardThemes {
    private val themes = listOf(
        BoardThemeSpec(
            id = "light",
            labelRes = R.string.board_theme_light,
            backgroundColorRes = R.color.white_background,
            deadCellDrawableRes = R.drawable.cell_white,
            liveCellDrawableRes = R.drawable.cell_blue,
        ),
        BoardThemeSpec(
            id = "dark",
            labelRes = R.string.board_theme_dark,
            backgroundColorRes = R.color.black_background,
            deadCellDrawableRes = R.drawable.cell_black,
            liveCellDrawableRes = R.drawable.cell_red,
        ),
    )

    fun all(): List<BoardThemeSpec> = themes

    fun default(): BoardThemeSpec = themes.first()

    fun findById(id: String?): BoardThemeSpec = themes.firstOrNull { it.id == id } ?: default()
}
