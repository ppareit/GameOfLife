package be.ppareit.gameoflife

import android.content.Context
import android.graphics.Matrix
import android.util.DisplayMetrics
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.WindowManager

class GameOfLifeTouchController(
    context: Context,
    private val getGame: () -> GameOfLife?,
    private val onBeforeGridMutation: () -> Unit,
    private val onInvalidate: () -> Unit,
) {
    private val appContext = context.applicationContext
    val drawMatrix = Matrix()
    private val moveDetector = GestureDetector(context, MoveListener())
    private val editListener = EditListener()
    private val scaleDetector = ScaleGestureDetector(context, ScaleListener())

    fun onSizeChanged(width: Int, height: Int) {
        drawMatrix.reset()
        val game = getGame() ?: return
        drawMatrix.postTranslate(-game.getCols() / 2f, -game.getRows() / 2f)

        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        (appContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getMetrics(metrics)
        val scale = INITIAL_CELL_SIZE_DP * metrics.density
        drawMatrix.postScale(scale, scale)
        Log.d(TAG, "Size changed, new scale: $scale")
        drawMatrix.postTranslate(width / 2f, height / 2f)
    }

    fun onTouchEvent(event: MotionEvent, state: GameOfLifeView.State) {
        when (state) {
            GameOfLifeView.State.EDITING -> {
                scaleDetector.onTouchEvent(event)
                if (!scaleDetector.isInProgress) {
                    editListener.onTouchEvent(event)
                }
            }
            GameOfLifeView.State.MOVING,
            GameOfLifeView.State.RUNNING -> {
                scaleDetector.onTouchEvent(event)
                moveDetector.onTouchEvent(event)
            }
        }
        onInvalidate()
    }

    private fun mapToGrid(x: Float, y: Float): Pair<Int, Int> {
        val pts = floatArrayOf(x, y)
        val inverse = Matrix()
        drawMatrix.invert(inverse)
        inverse.mapPoints(pts)
        return pts[1].toInt() to pts[0].toInt()
    }

    private fun getScale(): Float = drawMatrix.mapRadius(1f)

    private inner class MoveListener : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            drawMatrix.postTranslate(-distanceX, -distanceY)
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            val game = getGame() ?: return true
            val (row, col) = mapToGrid(e.x, e.y)
            if (row in 0 until game.getRows() && col in 0 until game.getCols()) {
                onBeforeGridMutation()
                game.grid[row][col] = if (game.grid[row][col] != 0) 0 else 1
            }
            return true
        }
    }

    private inner class EditListener {
        private var previousFlippedRow = -1
        private var previousFlippedCol = -1
        private var previousFlippedTime = 0L

        fun onTouchEvent(event: MotionEvent) {
            onBeforeGridMutation()
            for (index in 0 until event.historySize) {
                doEdit(event.getHistoricalX(index), event.getHistoricalY(index))
            }
            doEdit(event.x, event.y)
        }

        private fun doEdit(x: Float, y: Float) {
            val game = getGame() ?: return
            val (row, col) = mapToGrid(x, y)
            if (row in 0 until game.getRows() && col in 0 until game.getCols()) {
                val now = System.currentTimeMillis()
                if (previousFlippedCol == col && previousFlippedRow == row && previousFlippedTime + EDIT_DEBOUNCE_MS > now) {
                    return
                }
                previousFlippedCol = col
                previousFlippedRow = row
                previousFlippedTime = now
                game.grid[row][col] = if (game.grid[row][col] != 0) 0 else 1
            }
        }
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val factor = detector.scaleFactor
            val scale = factor * getScale()
            if (scale < MIN_CELL_SCALE || scale > MAX_CELL_SCALE) return false
            drawMatrix.postScale(factor, factor, detector.focusX, detector.focusY)
            Log.d(TAG, "New scale: ${getScale()}")
            return true
        }
    }

    companion object {
        private val TAG = GameOfLifeTouchController::class.java.simpleName
        private const val INITIAL_CELL_SIZE_DP = 20
        private const val MIN_CELL_SCALE = 10f
        private const val MAX_CELL_SCALE = 100f
        private const val EDIT_DEBOUNCE_MS = 500L
    }
}
