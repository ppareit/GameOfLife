package be.ppareit.gameoflife

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.drawable.Drawable
import androidx.core.graphics.withMatrix

class GameOfLifeRenderer(context: Context) {
    private val appContext = context.applicationContext
    private val canvasPaint = Paint().apply { color = Color.GRAY }
    private var backgroundPaint = Paint()
    private lateinit var liveCell: Drawable
    private lateinit var deadCell: Drawable

    init {
        updateTheme(BoardThemes.default())
    }

    fun updateTheme(theme: BoardThemeSpec) {
        backgroundPaint = Paint().apply {
            color = appContext.getColor(theme.backgroundColorRes)
        }
        deadCell = requireNotNull(appContext.getDrawable(theme.deadCellDrawableRes))
        liveCell = requireNotNull(appContext.getDrawable(theme.liveCellDrawableRes))
    }

    fun draw(canvas: Canvas, width: Int, height: Int, game: GameOfLife, drawMatrix: Matrix) {
        val rows = game.getRows()
        val cols = game.getCols()
        val grid = game.grid

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), canvasPaint)
        canvas.withMatrix(drawMatrix) {
            drawRect(0f, 0f, cols.toFloat(), rows.toFloat(), backgroundPaint)

            var left = 0
            var top = 0
            for (row in 0 until rows) {
                for (col in 0 until cols) {
                    val cell = if (grid[row][col] != 0) liveCell else deadCell
                    cell.setBounds(left, top, left + 1, top + 1)
                    cell.draw(this)
                    if (top + 1 > rows) {
                        cell.setBounds(left, top - rows, left + 1, top - rows + 1)
                        cell.draw(this)
                    }
                    left += 1
                }
                left = 0
                top += 1
            }
        }
    }
}
