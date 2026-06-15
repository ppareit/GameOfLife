package be.ppareit.gameoflife

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import be.ppareit.android.GameLoopView
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class GameOfLifeView(context: Context, attrs: AttributeSet?) : GameLoopView(context, attrs) {

    private val settingsRepository = App.settingsRepository
    private val settingsScope = MainScope()
    private var settingsJob: Job? = null
    private val renderer = GameOfLifeRenderer(context)
    private val touchController = GameOfLifeTouchController(
        context = context,
        getGame = { session?.game },
        onBeforeGridMutation = { session?.pushUndoState() },
        onInvalidate = { invalidate() },
    )
    var session: GameSessionViewModel? = null
        set(value) {
            if (field === value) return
            field = value
            if (width > 0 && height > 0) {
                touchController.onSizeChanged(width, height)
            }
            invalidate()
        }

    init {
        setTargetFps(GameSettings().animationSpeed)
    }

    fun setMode(mode: GameMode) {
        if (mode == GameMode.RUNNING) {
            startGameLoop()
        } else {
            pauseGameLoop()
        }
    }

    override fun onUpdate() {
        session?.game?.generateNextGeneration()
    }

    override fun onDraw(canvas: Canvas) {
        val game = session?.game ?: return
        renderer.draw(canvas, width, height, game, touchController.drawMatrix)
    }

    private fun applySettings(settings: GameSettings) {
        session?.game?.setUnderPopulation(settings.minimumVariable)
        session?.game?.setOverPopulation(settings.maximumVariable)
        session?.game?.setSpawn(settings.spawnVariable)
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
        touchController.onTouchEvent(event, session?.mode ?: GameMode.MOVING)
        return true
    }

    fun invalidateBoard() = invalidate()
}
