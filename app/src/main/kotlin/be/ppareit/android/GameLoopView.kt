package be.ppareit.android

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import be.ppareit.gameoflife.BuildConfig

abstract class GameLoopView(context: Context, attrs: AttributeSet?) : SurfaceView(context, attrs), SurfaceHolder.Callback {
    private var thread: AnimationThread? = null
    private var targetFps = 0
    private var drawFps = false
    @Volatile private var surfaceCreatedCompleted = false

    init {
        holder.addCallback(this)
        isFocusable = true
    }

    private inner class AnimationThread(private val surfaceHolder: SurfaceHolder) : Thread() {
        @Volatile private var running = false
        private var lastTime = System.currentTimeMillis()
        private var frameSamplesCollected = 0
        private var frameSampleTime = 0
        private var frameNumber = 0
        private var fps = 0
        private var nextGameTick = System.currentTimeMillis()
        private val fpsTextPaint = Paint().apply {
            setARGB(255, 255, 0, 0)
            textSize = 32f
        }

        @SuppressLint("WrongCall")
        override fun run() {
            Log.d(TAG, "AnimationThread running")
            while (!surfaceCreatedCompleted) {
                yield()
            }
            while (running) {
                onUpdate()
                val canvas = surfaceHolder.lockCanvas()
                if (canvas != null) {
                    try {
                        synchronized(surfaceHolder) {
                            onDraw(canvas)
                            drawFps(canvas)
                        }
                    } finally {
                        surfaceHolder.unlockCanvasAndPost(canvas)
                    }
                }
                sleepIfNeeded()
                frameNumber++
                updateFps()
            }
            Log.d(TAG, "AnimationThread stopped")
        }

        private fun sleepIfNeeded() {
            if (targetFps <= 0) return
            nextGameTick += 1000 / targetFps
            val sleepTime = nextGameTick - System.currentTimeMillis()
            if (sleepTime >= 0) {
                try {
                    sleep(sleepTime)
                } catch (e: InterruptedException) {
                    interrupt()
                }
            } else if (frameNumber % 20 == 0) {
                Log.i(TAG, "Failed to reach expected FPS")
            }
        }

        fun setRunning(state: Boolean) {
            running = state
        }

        private fun updateFps() {
            val currentTime = System.currentTimeMillis()
            val timeDifference = (currentTime - lastTime).toInt()
            frameSampleTime += timeDifference
            frameSamplesCollected++
            if (frameSamplesCollected == 10) {
                fps = (10 * 1000) / frameSampleTime.coerceAtLeast(1)
                frameSampleTime = 0
                frameSamplesCollected = 0
            }
            lastTime = currentTime
        }

        private fun drawFps(canvas: Canvas) {
            if (drawFps && fps != 0) {
                val x = width - width / 8f
                val y = height - fpsTextPaint.textSize - 5
                canvas.drawText("$fps fps", x, y, fpsTextPaint)
            }
        }
    }

    fun startGameLoop() {
        Log.d(TAG, "startGameLoop")
        if (thread == null) {
            thread = AnimationThread(holder).also {
                it.setRunning(true)
                it.start()
            }
        }
        if (BuildConfig.DEBUG) {
            drawFps = true
        }
    }

    fun pauseGameLoop() {
        thread?.let {
            it.setRunning(false)
            while (true) {
                try {
                    it.join()
                    break
                } catch (_: InterruptedException) {
                }
            }
            thread = null
        }
    }

    fun setTargetFps(fps: Int) {
        targetFps = fps
    }

    fun setDrawFps(show: Boolean) {
        drawFps = show
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) = Unit

    @SuppressLint("WrongCall")
    override fun surfaceCreated(holder: SurfaceHolder) {
        val canvas = holder.lockCanvas()
        if (canvas != null) {
            try {
                synchronized(holder) {
                    onDraw(canvas)
                }
            } finally {
                holder.unlockCanvasAndPost(canvas)
            }
        }
        surfaceCreatedCompleted = true
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        pauseGameLoop()
        surfaceCreatedCompleted = false
    }

    @SuppressLint("WrongCall")
    override fun invalidate() {
        val canvas = holder.lockCanvas()
        if (canvas != null) {
            try {
                synchronized(holder) {
                    onDraw(canvas)
                }
            } finally {
                holder.unlockCanvasAndPost(canvas)
            }
        }
    }

    protected abstract fun onUpdate()
    protected abstract override fun onDraw(canvas: Canvas)

    companion object {
        private val TAG = GameLoopView::class.java.simpleName
    }
}
