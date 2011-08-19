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
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;


/**
 * The main view for the Game of Life.
 * 
 * Responsible for drawing the data. Intercepts touch events. When the mode is set to
 * EDITING, it will add or remove data. When the mode is set to MOVING, panning and
 * zooming is done. When the mode is set to PLAYING, the game loop is started and the
 * data is updated to a new generation at each step.
 */
public class GameOfLifeView extends GameLoopView
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    

    public enum State {
        RUNNING,
        EDITING,
        MOVING,
        ZOOMING,
    }
    
    private State mState = State.EDITING;

    private static GameOfLife mGameOfLife = null;
    
    private Paint mCanvasPaint = null;    
    private Paint mBackgroundPaint = null;
    private Drawable mLiveCell = null;
    private Drawable mDeadCell = null;
    
    private int mXOffset = 0;
    private int mYOffset = 0;
    private int mCellSize = 0;

    public GameOfLifeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        if (mGameOfLife == null) {
            mGameOfLife = new GameOfLife(35, 65);
            mGameOfLife.setUnderPopulation(PreferencesActivity.getMinimumVariable(getContext()));
            mGameOfLife.setOverPopulation(PreferencesActivity.getMaximumVariable(getContext()));
            mGameOfLife.setSpawn(PreferencesActivity.getSpawnVariable(getContext()));
            mGameOfLife.loadGridFromFile(getResources().openRawResource(R.raw.android));
        }
        
        setTargetFps(PreferencesActivity.getAnimationSpeed(getContext()));
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.registerOnSharedPreferenceChangeListener(this);
        
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListner());

        initTheme(context);
    }
    
    private void initTheme(Context context) {
        int theme = PreferencesActivity.getDisplayTheme(context);
        Resources res = getResources();
        TypedArray themeData = res.obtainTypedArray(theme);
        
        mCanvasPaint = new Paint();
        mCanvasPaint.setColor(Color.GRAY);
        
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(themeData.getColor(0,Color.BLACK));
        
        mDeadCell = themeData.getDrawable(1);
        mLiveCell = themeData.getDrawable(2);
        mCellSize = mLiveCell.getMinimumHeight();
    }
    
    public void setMode(State mode) {
        if (mode == State.RUNNING && mState != State.RUNNING) {
            startGameLoop();
            mState = State.RUNNING;
        } else if (mode == State.EDITING && mState != State.EDITING) {
            pauseGameLoop();
            mState = State.EDITING;
        } else if (mode == State.MOVING && mState != State.MOVING) {
            pauseGameLoop();
            mState = State.MOVING;
        } else if (mode == State.ZOOMING && mState != State.ZOOMING) {
            pauseGameLoop();
            mState = State.ZOOMING;
        }
    }

    @Override
    protected void onUpdate() {
        mGameOfLife.generateNextGeneration();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawRect(0, 0, getWidth(), getHeight(), mCanvasPaint);
        
        canvas.save();
        canvas.translate(mXOffset, mYOffset);
        
        final int rows = mGameOfLife.getRows();
        final int cols = mGameOfLife.getCols();
        final int size = (int)(mCellSize*mScaleFactor);
        
        canvas.drawRect(0, 0, cols*size, rows*size, mBackgroundPaint);
        
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                
                int left = (int)(c*size);
                int top = (int)(r*size);
                int right = (int)(c*size + size);
                int bottom = (int)(r*size + size);
                
                if (mGameOfLife.getGrid()[r][c] != 0) {
                    mLiveCell.setBounds(left, top, right, bottom);
                    mLiveCell.draw(canvas);
                } else {
                    mDeadCell.setBounds(left, top, right, bottom);
                    mDeadCell.draw(canvas);
                }
                
            }
        }
        
        canvas.restore();
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("UNDERPOPULATION_VARIABLE")) {
            mGameOfLife.setUnderPopulation(PreferencesActivity.getMinimumVariable(getContext()));
        } else if (key.equals("OVERPOPULATION_VARIABLE")) {
            mGameOfLife.setOverPopulation(PreferencesActivity.getMaximumVariable(getContext()));
        } else if (key.equals("SPAWN_VARIABLE")) {
            mGameOfLife.setSpawn(PreferencesActivity.getSpawnVariable(getContext()));
        } else if (key.equals("ANIMATION_SPEED")) {
            setTargetFps(PreferencesActivity.getAnimationSpeed(getContext()));
        } else if (key.equals("DISPLAY_THEME")) {
            initTheme(getContext());
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mXOffset = (w - mGameOfLife.getCols()*mCellSize) / 2;
        mYOffset = (h - mGameOfLife.getRows()*mCellSize) / 2;

        super.onSizeChanged(w, h, oldw, oldh);
    }
    
    
    private static final int INVALID_POINTER_ID = -1;
    
    private int mActivePointerId = INVALID_POINTER_ID;
    private int mPreviousPanX;
    private int mPreviousPanY;
    
    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;
    
    private class ScaleListner extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            final float factor = detector.getScaleFactor();
            final float oldScaleFactor = mScaleFactor;
            
            mScaleFactor *= factor;
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.f));
            
            mXOffset += mGameOfLife.getCols()*mCellSize*(oldScaleFactor - mScaleFactor)/2;
            mYOffset += mGameOfLife.getRows()*mCellSize*(oldScaleFactor - mScaleFactor)/2;
            
            return true;
        }
        
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        
        switch (mState) {
        case RUNNING:
            return false;
        case EDITING:
            int historySize = event.getHistorySize();
            for (int h = 0; h < historySize; ++h) {
                doEdit((int)event.getHistoricalX(h), (int)event.getHistoricalY(h));
            }
            doEdit((int)event.getX(), (int)event.getY());
            break;
        case MOVING:
            mScaleDetector.onTouchEvent(event);
            if (mScaleDetector.isInProgress()) break;
            
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                mPreviousPanX = (int)event.getX();
                mPreviousPanY = (int)event.getY();
                mActivePointerId = event.getPointerId(0);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                final int ndx = event.findPointerIndex(mActivePointerId);
                if (ndx == -1) {
                    mActivePointerId = INVALID_POINTER_ID;
                    return false;
                }
                
                final int x = (int)event.getX(ndx);
                final int y = (int)event.getY(ndx);
                
                mXOffset += (x - mPreviousPanX);
                mYOffset += (y - mPreviousPanY);

                mPreviousPanX = x;
                mPreviousPanY = y;
                break;
            }
            case MotionEvent.ACTION_UP: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            } 
            case MotionEvent.ACTION_CANCEL: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                final int action = event.getAction();
                final int mask = MotionEvent.ACTION_POINTER_INDEX_MASK;
                final int shift = MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int ndx = (action & mask) >> shift;
            
                final int pid = event.getPointerId(ndx);
                
                if (pid == mActivePointerId) {
                    final int newPointerIndex = (pid == 0) ? 1 : 0;
                    mPreviousPanX = (int)event.getX(newPointerIndex);
                    mPreviousPanY = (int)event.getY(newPointerIndex);
                    mActivePointerId = event.getPointerId(newPointerIndex);
                }
                break;
            }
            }
            break;
        }

        invalidate();
        return true;
    }
    
    private int mPreviousFlippedRow = -1;
    private int mPreviousFlippedCol = -1;
    private long mPreviousFlippedTime = 0;
    
    private void doEdit(int x, int y) {
        
        final int cols = mGameOfLife.getCols();
        final int rows = mGameOfLife.getRows();
        final int size = (int)(mCellSize*mScaleFactor);
        
        if (x <= mXOffset || mXOffset + cols*size <= x) return;
        if (y <= mYOffset || mYOffset + rows*size <= y) return;

        int c = ((x - mXOffset)/size) % cols;
        int r = ((y - mYOffset)/size) % rows;
        
        if (mPreviousFlippedCol == c && mPreviousFlippedRow == r &&
                mPreviousFlippedTime + 500 > System.currentTimeMillis()) {
            return;
        } else {
            mPreviousFlippedCol = c;
            mPreviousFlippedRow = r;
            mPreviousFlippedTime = System.currentTimeMillis();
        }

        mGameOfLife.getGrid()[r][c] = ( mGameOfLife.getGrid()[r][c] != 0 ? 0 : 1);
    }

    public void clearGrid() {
        mGameOfLife.resetGrid();
        invalidate();
    }
    
    
}





























