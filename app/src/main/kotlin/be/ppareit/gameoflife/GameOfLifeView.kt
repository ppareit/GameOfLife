package be.ppareit.gameoflife

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.net.Uri
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.Toast
import be.ppareit.android.GameLoopView
import be.ppareit.gameoflife.patterns.PatternFormatException
import java.io.FileNotFoundException
import java.io.InputStream
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class GameOfLifeView(context: Context, attrs: AttributeSet?) : GameLoopView(context, attrs) {

    enum class State {
        RUNNING,
        EDITING,
        MOVING,
    }

    private val settingsRepository = App.settingsRepository
    private val settingsScope = MainScope()
    private var settingsJob: Job? = null
    private var state = State.MOVING
    private val renderer = GameOfLifeRenderer(context)
    private val touchController = GameOfLifeTouchController(
        context = context,
        getGame = { gameOfLife },
        onBeforeGridMutation = { pushUndoState() },
        onInvalidate = { invalidate() },
    )
    var onUndoStateChanged: ((Boolean) -> Unit)? = null

    init {
        val settings = GameSettings()
        if (gameOfLife == null) {
            gameOfLife = GameOfLife(settings.rows, settings.cols).also {
                it.setUnderPopulation(settings.minimumVariable)
                it.setOverPopulation(settings.maximumVariable)
                it.setSpawn(settings.spawnVariable)
                it.loadGridFromFile(resources.openRawResource(R.raw.android))
            }
            undoManager = UndoManager(gameOfLife!!)
        }

        setTargetFps(settings.animationSpeed)
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
        renderer.draw(canvas, width, height, game, touchController.drawMatrix)
    }

    private fun applySettings(settings: GameSettings) {
        gameOfLife?.setUnderPopulation(settings.minimumVariable)
        gameOfLife?.setOverPopulation(settings.maximumVariable)
        gameOfLife?.setSpawn(settings.spawnVariable)
        setTargetFps(settings.animationSpeed)
        renderer.updateTheme(settings.displayTheme)
        invalidate()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (settingsJob?.isActive == true) return
        settingsJob = settingsScope.launch {
            settingsRepository.settings.collect { applySettings(it) }
        }
    }

    override fun onDetachedFromWindow() {
        settingsJob?.cancel()
        settingsJob = null
        super.onDetachedFromWindow()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        touchController.onSizeChanged(w, h)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        touchController.onTouchEvent(event, state)
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
        private var gameOfLife: GameOfLife? = null
        private var undoManager: UndoManager? = null
    }
}
