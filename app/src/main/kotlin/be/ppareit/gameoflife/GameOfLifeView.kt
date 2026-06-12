package be.ppareit.gameoflife

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.WindowManager
import android.widget.Toast
import androidx.core.graphics.withMatrix
import be.ppareit.android.GameLoopView
import be.ppareit.gameoflife.patterns.PatternFormatException
import java.io.FileNotFoundException
import java.io.InputStream

class GameOfLifeView(context: Context, attrs: AttributeSet?) : GameLoopView(context, attrs),
    SharedPreferences.OnSharedPreferenceChangeListener {

    enum class State {
        RUNNING,
        EDITING,
        MOVING,
    }

    private var state = State.MOVING
    private var canvasPaint = Paint().apply { color = Color.GRAY }
    private var backgroundPaint = Paint()
    private lateinit var liveCell: Drawable
    private lateinit var deadCell: Drawable
    private val moveDetector = GestureDetector(context, MoveListener())
    private val editListener = EditListener()
    private val scaleDetector = ScaleGestureDetector(context, ScaleListener())
    private val drawMatrix = Matrix()
    var onUndoStateChanged: ((Boolean) -> Unit)? = null

    init {
        val settings = Settings.getSettings(context)
        if (gameOfLife == null) {
            gameOfLife = GameOfLife(settings.rows, settings.cols).also {
                it.setUnderPopulation(settings.getMinimumVariable())
                it.setOverPopulation(settings.getMaximumVariable())
                it.setSpawn(settings.getSpawnVariable())
                it.loadGridFromFile(resources.openRawResource(R.raw.android))
            }
            undoManager = UndoManager(gameOfLife!!)
        }

        setTargetFps(settings.getAnimationSpeed())
        Settings.getPreferences(context).registerOnSharedPreferenceChangeListener(this)
        initTheme(context)
    }

    private fun initTheme(context: Context) {
        val theme = Settings.getSettings(context).getDisplayTheme()
        backgroundPaint = Paint().apply {
            color = context.getColor(theme.backgroundColorRes)
        }
        deadCell = requireNotNull(context.getDrawable(theme.deadCellDrawableRes))
        liveCell = requireNotNull(context.getDrawable(theme.liveCellDrawableRes))
    }

    fun setMode(mode: State) {
        if (mode == State.RUNNING && state != State.RUNNING) {
            pushUndoState()
            startGameLoop()
            state = State.RUNNING
        } else if (mode == State.EDITING && state != State.EDITING) {
            pauseGameLoop()
            state = State.EDITING
        } else if (mode == State.MOVING && state != State.MOVING) {
            pauseGameLoop()
            state = State.MOVING
        }
    }

    fun getGameState(): State = state

    override fun onUpdate() {
        gameOfLife?.generateNextGeneration()
    }

    override fun onDraw(canvas: Canvas) {
        val game = gameOfLife ?: return
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

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        val settings = Settings.getSettings(context)
        gameOfLife?.setUnderPopulation(settings.getMinimumVariable())
        gameOfLife?.setOverPopulation(settings.getMaximumVariable())
        gameOfLife?.setSpawn(settings.getSpawnVariable())
        setTargetFps(settings.getAnimationSpeed())
        initTheme(context)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        drawMatrix.reset()
        val game = gameOfLife ?: return
        drawMatrix.postTranslate(-game.getCols() / 2f, -game.getRows() / 2f)

        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getMetrics(metrics)
        val scale = 20 * metrics.density
        drawMatrix.postScale(scale, scale)
        Log.d(TAG, "Size changed, new scale: $scale")
        drawMatrix.postTranslate(width / 2f, height / 2f)
    }

    private inner class MoveListener : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            drawMatrix.postTranslate(-distanceX, -distanceY)
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            val game = gameOfLife ?: return true
            val pts = floatArrayOf(e.x, e.y)
            val inverse = Matrix()
            drawMatrix.invert(inverse)
            inverse.mapPoints(pts)
            val row = pts[1].toInt()
            val col = pts[0].toInt()
            if (row in 0 until game.getRows() && col in 0 until game.getCols()) {
                pushUndoState()
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
            pushUndoState()
            for (index in 0 until event.historySize) {
                doEdit(event.getHistoricalX(index).toInt(), event.getHistoricalY(index).toInt())
            }
            doEdit(event.x.toInt(), event.y.toInt())
        }

        private fun doEdit(x: Int, y: Int) {
            val game = gameOfLife ?: return
            val pts = floatArrayOf(x.toFloat(), y.toFloat())
            val inverse = Matrix()
            drawMatrix.invert(inverse)
            inverse.mapPoints(pts)
            val col = pts[0].toInt()
            val row = pts[1].toInt()
            if (row in 0 until game.getRows() && col in 0 until game.getCols()) {
                val now = System.currentTimeMillis()
                if (previousFlippedCol == col && previousFlippedRow == row && previousFlippedTime + 500 > now) {
                    return
                }
                previousFlippedCol = col
                previousFlippedRow = row
                previousFlippedTime = now
                game.grid[row][col] = if (game.grid[row][col] != 0) 0 else 1
            }
        }
    }

    private fun getScale(): Float = drawMatrix.mapRadius(1f)

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val factor = detector.scaleFactor
            val scale = factor * getScale()
            if (scale < 10f || scale > 100f) return false
            drawMatrix.postScale(factor, factor, detector.focusX, detector.focusY)
            Log.d(TAG, "New scale: ${getScale()}")
            return true
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (state) {
            State.EDITING -> {
                scaleDetector.onTouchEvent(event)
                if (!scaleDetector.isInProgress) {
                    editListener.onTouchEvent(event)
                }
            }
            State.MOVING,
            State.RUNNING -> {
                scaleDetector.onTouchEvent(event)
                moveDetector.onTouchEvent(event)
            }
        }
        invalidate()
        return true
    }

    fun clearGrid() {
        pushUndoState()
        gameOfLife?.resetGrid()
        invalidate()
    }

    fun doSingleStep() {
        pushUndoState()
        gameOfLife?.generateNextGeneration()
        invalidate()
    }

    fun canUndo(): Boolean = undoManager?.canUndo() == true

    fun doUndo() {
        undoManager?.popState()
        notifyUndoStateChanged()
        invalidate()
    }

    fun doLoad(inputStream: InputStream) {
        gameOfLife?.loadGridFromFile(inputStream)
        notifyUndoStateChanged()
        invalidate()
    }

    fun doLoad(uri: Uri) {
        try {
            context.contentResolver.openInputStream(uri).use { input ->
                if (input == null) throw FileNotFoundException()
                gameOfLife?.loadGridFromFile(input)
            }
            notifyUndoStateChanged()
            invalidate()
        } catch (_: FileNotFoundException) {
            Toast.makeText(context, R.string.file_not_found, Toast.LENGTH_SHORT).show()
        } catch (_: PatternFormatException) {
            Toast.makeText(context, R.string.file_format_not_supported, Toast.LENGTH_LONG).show()
        }
    }

    fun doSave(uri: Uri) {
        try {
            context.contentResolver.openOutputStream(uri).use { output ->
                if (output == null) throw FileNotFoundException()
                gameOfLife?.saveGridToFile(output)
            }
        } catch (_: FileNotFoundException) {
            Toast.makeText(context, R.string.file_not_found, Toast.LENGTH_SHORT).show()
        }
    }

    private fun pushUndoState() {
        undoManager?.pushState()
        notifyUndoStateChanged()
    }

    private fun notifyUndoStateChanged() {
        onUndoStateChanged?.invoke(canUndo())
    }

    companion object {
        private val TAG = GameOfLifeView::class.java.simpleName
        private var gameOfLife: GameOfLife? = null
        private var undoManager: UndoManager? = null
    }
}
