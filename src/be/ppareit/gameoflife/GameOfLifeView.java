/*******************************************************************************
 * Copyright (c) 2011-2013 Pieter Pareit.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;
import be.ppareit.android.GameLoopView;
import be.ppareit.gameoflife.R;

/**
 * The main view for the Game of Life.
 *
 * Responsible for drawing the data. Intercepts touch events. When the mode is set to
 * EDITING, it will add or remove data. When the mode is set to MOVING, panning and
 * zooming is done. When the mode is set to PLAYING, the game loop is started and the data
 * is updated to a new generation at each step.
 */
public class GameOfLifeView extends GameLoopView implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = GameOfLifeView.class.getSimpleName();

    public enum State {
        RUNNING, EDITING, MOVING,
    }

    private State mState = State.MOVING;

    private static GameOfLife mGameOfLife = null;
    private static UndoManager mUndoManager = null;

    private Paint mCanvasPaint = null;
    private Paint mBackgroundPaint = null;
    private Drawable mLiveCell = null;
    private Drawable mDeadCell = null;

    // this object detects move and single tap events
    private final GestureDetector mMoveDetector;

    // this object detects move events, and continuously toggles cell state
    private final EditListner mEditListner;

    private int mXOffset = 0;
    private int mYOffset = 0;

    // this object can detect pinch to zoom touch events
    private final ScaleGestureDetector mScaleDetector;

    // zooming factor, set with pinch to zoom gesture
    private float mScale = Float.MAX_VALUE;
    private float mMaxScale = Float.MAX_VALUE;

    public GameOfLifeView(Context context, AttributeSet attrs) {
        super(context, attrs);

        Settings settings = Settings.getSettings(context);

        if (mGameOfLife == null) {
            int rows = settings.getRows();
            int cols = settings.getCols();

            mGameOfLife = new GameOfLife(rows, cols);
            mGameOfLife.setUnderPopulation(settings.getMinimumVariable());
            mGameOfLife.setOverPopulation(settings.getMaximumVariable());
            mGameOfLife.setSpawn(settings.getSpawnVariable());
            mGameOfLife.loadGridFromFile(getResources().openRawResource(R.raw.android));

            mUndoManager = new UndoManager(mGameOfLife);
        }

        setTargetFps(settings.getAnimationSpeed());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.registerOnSharedPreferenceChangeListener(this);

        mEditListner = new EditListner();
        mMoveDetector = new GestureDetector(context, new MoveListner());
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListner());

        initTheme(context);
    }

    private void initTheme(Context context) {
        Settings settings = Settings.getSettings(context);
        int theme = settings.getDisplayTheme();
        Resources res = getResources();
        TypedArray themeData = res.obtainTypedArray(theme);

        mCanvasPaint = new Paint();
        mCanvasPaint.setColor(Color.GRAY);

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(themeData.getColor(0, Color.BLACK));

        mDeadCell = themeData.getDrawable(1);
        mLiveCell = themeData.getDrawable(2);
    }

    public void setMode(State mode) {
        if (mode == State.RUNNING && mState != State.RUNNING) {
            mUndoManager.pushState();
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

        final int rows = mGameOfLife.getRows();
        final int cols = mGameOfLife.getCols();

        final float scale = mScale;
        final int rightEdge = (int) (cols - mXOffset / scale);
        final int bottomEdge = (int) (rows - mYOffset / scale);

        final int[][] grid = mGameOfLife.getGrid();

        canvas.drawRect(0, 0, getWidth(), getHeight(), mBackgroundPaint);

        canvas.save();

        canvas.translate(mXOffset, mYOffset);
        canvas.scale(scale, scale);

        // addition is faster then multiplication combined with modulo,
        // so keep track of correct drawing position and update it every step
        int left = 0;
        int top = 0;
        // for all rows and all cols
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                // draw the cell
                final Drawable cell = (grid[r][c] != 0) ? mLiveCell : mDeadCell;
                cell.setBounds(left, top, left + 1, top + 1);
                cell.draw(canvas);
                // check if at bound
                if (top + 1 > rows) {
                    // redraw cell, but at bottom
                    cell.setBounds(left, top - rows, left + 1, top - rows + 1);
                    cell.draw(canvas);
                }
                // reposition left
                left += 1;
                // if going over the edge
                if (left > rightEdge) {
                    // reposition
                    left -= cols + 1;
                    c--;
                }
            }
            left = 0;
            top += 1;
            // if going over the edge
            if (top > bottomEdge) {
                // move back and one more so that one is redrawn
                top -= rows + 1;
                r--;
            }
        }
        canvas.restore();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Settings settings = Settings.getSettings(getContext());

        mGameOfLife.setUnderPopulation(settings.getMinimumVariable());
        mGameOfLife.setOverPopulation(settings.getMaximumVariable());
        mGameOfLife.setSpawn(settings.getSpawnVariable());

        setTargetFps(settings.getAnimationSpeed());

        initTheme(getContext());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        final float sx = (float) w / mGameOfLife.getCols();
        final float sy = (float) h / mGameOfLife.getRows();

        mMaxScale = Math.max(sx, sy);
        mScale = mScale > mMaxScale ? mMaxScale : mScale;

        Log.d(TAG, "New scale: " + mScale);
    }

    private class MoveListner extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                float distanceY) {
            final int cols = mGameOfLife.getCols();
            final int rows = mGameOfLife.getRows();
            final int width = (int) (cols * mScale);
            final int height = (int) (rows * mScale);
            mXOffset = (int) (mXOffset - distanceX + width) % width;
            mYOffset = (int) (mYOffset - distanceY + height) % height;
            return true;
        }

        /**
         * Even in the MOVING state we provide some editing, this by single tapping
         */
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            final int x = (int) e.getX();
            final int y = (int) e.getY();
            final int cols = mGameOfLife.getCols();
            final int rows = mGameOfLife.getRows();
            final float scale = mScale;

            int c = (int) ((x - mXOffset + cols * scale) / scale) % cols;
            int r = (int) ((y - mYOffset + rows * scale) / scale) % rows;

            mUndoManager.pushState();

            mGameOfLife.getGrid()[r][c] = (mGameOfLife.getGrid()[r][c] != 0 ? 0 : 1);

            return true;
        }

    }

    private class EditListner {

        public void onTouchEvent(MotionEvent event) {
            mUndoManager.pushState();
            int historySize = event.getHistorySize();
            for (int h = 0; h < historySize; ++h) {
                doEdit((int) event.getHistoricalX(h), (int) event.getHistoricalY(h));
            }
            doEdit((int) event.getX(), (int) event.getY());
        }

        private int mPreviousFlippedRow = -1;
        private int mPreviousFlippedCol = -1;
        private long mPreviousFlippedTime = 0;

        private void doEdit(int x, int y) {

            final int cols = mGameOfLife.getCols();
            final int rows = mGameOfLife.getRows();
            final float scale = mScale;

            int c = (int) ((x - mXOffset + cols * scale) / scale) % cols;
            int r = (int) ((y - mYOffset + rows * scale) / scale) % rows;

            if (mPreviousFlippedCol == c && mPreviousFlippedRow == r
                    && mPreviousFlippedTime + 500 > System.currentTimeMillis()) {
                return;
            } else {
                mPreviousFlippedCol = c;
                mPreviousFlippedRow = r;
                mPreviousFlippedTime = System.currentTimeMillis();
            }

            mGameOfLife.getGrid()[r][c] = (mGameOfLife.getGrid()[r][c] != 0 ? 0 : 1);
        }

    }

    /**
     * Callback for the ScaleGestureDetector
     */
    private class ScaleListner extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            final int cols = mGameOfLife.getCols();
            final int rows = mGameOfLife.getRows();

            final float factor = detector.getScaleFactor();
            final float focusX = detector.getFocusX();
            final float focusY = detector.getFocusY();

            final float oldScale = mScale;
            final float newScale = Math
                    .max(mMaxScale, Math.min(oldScale * factor, 200.f));

            final float width = cols * newScale;
            final float height = rows * newScale;

            final float focusXdiff = focusX - width / 2;
            final float focusYdiff = focusY - height / 2;

            mXOffset += focusXdiff * (oldScale - newScale) / 2;
            mYOffset += focusYdiff * (oldScale - newScale) / 2;

            mXOffset %= width;
            mYOffset %= height;

            mScale = newScale;

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
            // Let pinch to zoom process the event
            mScaleDetector.onTouchEvent(event);
            // give the gesture detector the event
            mMoveDetector.onTouchEvent(event);

            break;
        }

        invalidate();
        return true;
    }

    public void clearGrid() {
        mUndoManager.pushState();
        mGameOfLife.resetGrid();
        invalidate();
    }

    public void doSingleStep() {
        mUndoManager.pushState();
        mGameOfLife.generateNextGeneration();
        invalidate();
    }

    public boolean canUndo() {
        return mUndoManager.canUndo();
    }

    public void doUndo() {
        mUndoManager.popState();
        invalidate();
    }

    public void doLoad(Uri uri) {
        Context context = getContext();
        try {
            File file = new File(uri.getPath());
            InputStream is;
            is = new FileInputStream(file);
            mGameOfLife.loadGridFromFile(is);
        } catch (FileNotFoundException e) {
            Toast.makeText(context, R.string.file_not_found, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (GameOfLife.FormatNotSupportedException e) {
            Toast.makeText(context, R.string.file_format_not_supported, Toast.LENGTH_LONG)
                    .show();
            e.printStackTrace();
        }
    }

}
