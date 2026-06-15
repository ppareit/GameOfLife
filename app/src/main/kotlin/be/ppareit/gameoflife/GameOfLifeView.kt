package be.ppareit.gameoflife

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import be.ppareit.android.GameLoopView

class GameOfLifeView(
    context: Context,
    attrs: AttributeSet?,
    initialSettings: GameSettings,
) : GameLoopView(context, attrs) {

    private val renderer = GameOfLifeRenderer(context, initialSettings.displayTheme)
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
        }

    init {
        setTargetFps(initialSettings.animationSpeed)
    }

    fun applySettings(settings: GameSettings) {
        session?.game?.setUnderPopulation(settings.minimumVariable)
        session?.game?.setOverPopulation(settings.maximumVariable)
        session?.game?.setSpawn(settings.spawnVariable)
        setTargetFps(settings.animationSpeed)
        renderer.updateTheme(settings.displayTheme)
        invalidate()
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
