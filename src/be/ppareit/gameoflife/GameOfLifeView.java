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
import android.view.GestureDetector;
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
    }
    
    private State mState = State.MOVING;

    private static GameOfLife mGameOfLife = null;
    
    private Paint mCanvasPaint = null;    
    private Paint mBackgroundPaint = null;
    private Drawable mLiveCell = null;
    private Drawable mDeadCell = null;
    
    // this object detects move and single tap events
    private GestureDetector mMoveDetector;
    
    // this object detects move events, and continuously toggles cell state
    private EditListner mEditListner;
    
    private int mXOffset = 0;
    private int mYOffset = 0;
    
    // this object can detect pinch to zoom touch events
    private ScaleGestureDetector mScaleDetector;
    
    // stretch the blocks to the complete display
    private float mScaleX = 1.f;
    private float mScaleY = 1.f;
    // zooming factor, set with pinch to zoom gesture
    private float mScaleFactor = 1.f;

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

        mEditListner = new EditListner();
        mMoveDetector = new GestureDetector(context, new MoveListner());
        
        // construct the pinch to zoom detector with our own callback
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
        }
    }

    @Override
    protected void onUpdate() {
        mGameOfLife.generateNextGeneration();
    }
    
    /**
     * @todo this can faster, just draw what needs to be drawn in case we are zoomed in
     */
    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawRect(0, 0, getWidth(), getHeight(), mCanvasPaint);
        
        final int rows = mGameOfLife.getRows();
        final int cols = mGameOfLife.getCols();
        
        final float scaleX = mScaleX*mScaleFactor;
        final float scaleY = mScaleY*mScaleFactor;
        
        canvas.drawRect(0, 0, cols*scaleX, rows*scaleY, mBackgroundPaint);
        
        // addition is faster then multiplication combined with modulo,
        // so keep track of correct drawing position and update it every step
        float left = mXOffset;
        float top = mYOffset;
        // for all rows and all cols
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                // draw the cell
                final Drawable cell = (mGameOfLife.getGrid()[r][c] != 0) ?
                        mLiveCell : mDeadCell;
                cell.setBounds((int)left, (int)top,
                        (int)(left + scaleX), (int)(top + scaleY));
                cell.draw(canvas);
                // check if at bound
                if (top + scaleX > rows*scaleY) {
                    // redraw cell, but at bottom
                    cell.setBounds((int)left, (int)(top-rows*scaleY),
                            (int)(left + scaleX), (int)(top-rows*scaleY+scaleY));
                    cell.draw(canvas);
                }
                // reposition left
                left += scaleX;
                // if going over the edge
                if (left > cols*scaleX) {
                    // reposition
                    left -= cols*scaleX;
                    // draw an extra cell to the left
                    cell.setBounds((int)(left - scaleX), (int)top,
                            (int)left, (int)(top + scaleY));
                    cell.draw(canvas);
                    // if in left bottom corner
                    if (top + scaleY > rows*scaleY) {
                        // draw an extra cell in the left top corner
                        cell.setBounds((int)(left - scaleX), (int)(top - rows*scaleY),
                                (int)left, (int)(top-rows*scaleY+scaleY));
                        cell.draw(canvas);
                    }
                }
            }
            left = mXOffset;
            top += scaleY;
            if (top > rows*scaleY) top -= rows*scaleY;
        }
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
        mXOffset = 0;
        mYOffset = 0;
        mScaleFactor = 1.f;
        
        mScaleX = (float)w/mGameOfLife.getCols();
        mScaleY = (float)h/mGameOfLife.getRows();
    }
    
    private class MoveListner extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                float distanceX, float distanceY) {
            final int width = (int)(mGameOfLife.getCols()*mScaleX*mScaleFactor);
            final int height = (int)(mGameOfLife.getRows()*mScaleY*mScaleFactor);
            mXOffset = (int)(mXOffset - distanceX + width) % width;
            mYOffset = (int)(mYOffset - distanceY + height) % height;
            return true;
        }

        /**
         * Even in the MOVING state we provide some editing, this by single tapping
         */
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            final int x = (int)e.getX();
            final int y = (int)e.getY();
            final int cols = mGameOfLife.getCols();
            final int rows = mGameOfLife.getRows();
            final float scaleX = mScaleX*mScaleFactor;
            final float scaleY = mScaleY*mScaleFactor;
            
            int c = (int)((x - mXOffset + cols*scaleX)/scaleX) % cols;
            int r = (int)((y - mYOffset + rows*scaleY)/scaleY) % rows;
            
            mGameOfLife.getGrid()[r][c] = ( mGameOfLife.getGrid()[r][c] != 0 ? 0 : 1);
            
            return true;
        }
        
    }
    
    private class EditListner{

        public void onTouchEvent(MotionEvent event) {
            int historySize = event.getHistorySize();
            for (int h = 0; h < historySize; ++h) {
                doEdit((int)event.getHistoricalX(h), (int)event.getHistoricalY(h));
            }
            doEdit((int)event.getX(), (int)event.getY());
        }
        
        private int mPreviousFlippedRow = -1;
        private int mPreviousFlippedCol = -1;
        private long mPreviousFlippedTime = 0;
        
        private void doEdit(int x, int y) {
            
            final int cols = mGameOfLife.getCols();
            final int rows = mGameOfLife.getRows();
            final float scaleX = mScaleX*mScaleFactor;
            final float scaleY = mScaleY*mScaleFactor;
            
            int c = (int)((x - mXOffset + cols*scaleX)/scaleX) % cols;
            int r = (int)((y - mYOffset + rows*scaleY)/scaleY) % rows;
            
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

    }
    
    /**
     * Callback for the ScaleGestureDetector
     */
    private class ScaleListner extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            final float factor = detector.getScaleFactor();
            final float oldScaleFactor = mScaleFactor;
            
            mScaleFactor *= factor;
            mScaleFactor = Math.max(1.f, Math.min(mScaleFactor, 5.f));
            
            // zoom from the center of the screen
            // TODO: it is better to zoom from the center of the two fingers 
            mXOffset += mGameOfLife.getCols()*(oldScaleFactor - mScaleFactor)/2;
            mYOffset += mGameOfLife.getRows()*(oldScaleFactor - mScaleFactor)/2;
            
            return true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        
        switch (mState) {
        case RUNNING:
            return false;
        case EDITING:
            
            mEditListner.onTouchEvent(event);
            break;
        case MOVING:
            // give the pinch to zoom detector the event and possible stop here
            mScaleDetector.onTouchEvent(event);
            if (mScaleDetector.isInProgress()) break;
            
            // give the gesture detector the event
            mMoveDetector.onTouchEvent(event);
            
            break;
        }

        invalidate();
        return true;
    }
    
    public void clearGrid() {
        mGameOfLife.resetGrid();
        invalidate();
    }

    public void doSingleStep() {
        mGameOfLife.generateNextGeneration();
        invalidate();
    }
    
}





























