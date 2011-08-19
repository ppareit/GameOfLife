/*******************************************************************************
 * Copyright (c) 2011 Pieter Pareit.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Pieter Pareit - initial API and implementation
 ******************************************************************************/
package be.ppareit.gameoflife;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


/**
 * This class contains all logic related to running a game loop.<p>
 * 
 * This class must be extended by your game. All game logic should happen in
 * onUpdate(). All drawing should happen in onDraw(). The game loop is started with
 * startGameLoop() and stopped with pauseGameLoop(). When the game loop is paused, and
 * the screen needs to be refreshed, call invalidate(). <p>
 * 
 * While part of GameOfLife, this class can be reused in other applications that need
 * a view containing a game loop and the related logic.
 * 
 */
public abstract class GameLoopView extends SurfaceView implements SurfaceHolder.Callback {

    class AnimationThread extends Thread {
        
        private boolean mRun;
        
        private long mLastTime = System.currentTimeMillis();
        
        private int mFrameSamplesCollected = 0;
        private int mFrameSampleTime = 0;
        private int mFps = 0;
        
        private SurfaceHolder mSurfaceHolder;
        
        private Paint mFpsTextPaint;
        
        public AnimationThread(SurfaceHolder surfaceHolder) {
            mSurfaceHolder = surfaceHolder;
            
            mFpsTextPaint = new Paint();
            mFpsTextPaint.setARGB(255, 255, 0, 0);
            mFpsTextPaint.setTextSize(32);
        }
        
        @Override
        public void run() {
            while (mRun) {
                onUpdate();
                Canvas canvas = null;
                try {
                    canvas = mSurfaceHolder.lockCanvas();
                    synchronized (mSurfaceHolder) {
                        onDraw(canvas);
                        //drawFps(canvas);
                    }
                } finally {
                        mSurfaceHolder.unlockCanvasAndPost(canvas);
                }
                sleepIfNeeded();
                updateFps();
            }
        }
        
        private long mNextGameTick = System.currentTimeMillis();
        
        private void sleepIfNeeded() {
            mNextGameTick += 1000 / mTargetFps;
            long sleepTime = mNextGameTick - System.currentTimeMillis();
            if (sleepTime >= 0) {
                try {
                    sleep(sleepTime);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                Log.i("GameLoopView", "Failed to reach expected FPS!");
            }
        }
        
        public void setRunning(boolean state) {
            mRun = state;
        }
        
        private void updateFps() {
            long currentTime = System.currentTimeMillis();
            
            int timeDifference = (int)(currentTime - mLastTime);
            mFrameSampleTime += timeDifference;
            mFrameSamplesCollected++;
            
            if (mFrameSamplesCollected == 10) {
                mFps = (int)((10*1000) / mFrameSampleTime);
                
                mFrameSampleTime = 0;
                mFrameSamplesCollected = 0;
            }
            
            mLastTime = currentTime;
        }
        
        @SuppressWarnings("unused")
        private void drawFps(Canvas canvas) {
            if (mFps != 0) {
                int x = getWidth() - getWidth() / 8;
                int y = getHeight() - (int)mFpsTextPaint.getTextSize() - 5;
                canvas.drawText(mFps + " fps", x, y, mFpsTextPaint);
            }
        }
    }
    
    private AnimationThread mThread;
    private int mTargetFps = 10;
    
    public GameLoopView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        getHolder().addCallback(this);

        setFocusable(true);
    }
    
    public void startGameLoop() {
        // if thread exists, the gameloop is running
        if (mThread == null) {
            mThread = new AnimationThread(getHolder());
            mThread.setRunning(true);
            mThread.start();
        }
    }
    
    public void pauseGameLoop() {
        // only pause a gameloop that is running
        if (mThread != null) {
            boolean retry = true;
            mThread.setRunning(false);
            while (retry) {
                try {
                    mThread.join();
                    retry = false;
                } catch (InterruptedException e) {
                    // swallow exception and retry joining thread
                }
            }
            mThread = null;
        }
    }

    
    /**
     * Set's the frame rate at which the game loop should run. Be conservative and
     * implement an efficient onUpate()/onDraw() so this frame rate can be maintaned. 
     * 
     * @param fps The frame rate at which the game loop should run.
     */
    public void setTargetFps(int fps) {
        mTargetFps = fps;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Canvas canvas = null;
        try {
            canvas = holder.lockCanvas();
            synchronized (holder) {
                onDraw(canvas);
            }
        } finally {
            holder.unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        
    }
    
    @Override
    public void invalidate() {
        SurfaceHolder holder = getHolder();
        Canvas canvas = null;
        try {
            canvas = holder.lockCanvas();
            synchronized (holder) {
                onDraw(canvas);
            }
        } finally {
            holder.unlockCanvasAndPost(canvas);
        }
    }

    abstract protected void onUpdate();
    
    abstract protected void onDraw(Canvas canvas);

}
















