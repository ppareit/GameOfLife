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

import java.util.ArrayList;
import java.util.List;

public class UndoManager {

    private final GameOfLife mGameOfLife;

    List<GameOfLifeState> mUndoStack = new ArrayList<GameOfLifeState>();

    private class GameOfLifeState {

        private final int [][] mGrid;

        public GameOfLifeState() {
            mGrid = new int [mGameOfLife.getRows()][mGameOfLife.getCols()];
            for (int r = 0; r < mGameOfLife.getRows(); ++r)
                for (int c = 0; c < mGameOfLife.getCols(); ++c)
                    mGrid[r][c] = mGameOfLife.getGrid()[r][c];
        }

        public void restore() {
            for (int r = 0; r < mGameOfLife.getRows(); ++r)
                for (int c = 0; c < mGameOfLife.getCols(); ++c)
                    mGameOfLife.getGrid()[r][c] = mGrid[r][c];
        }

        public boolean stateChanged() {
            for (int r = 0; r < mGameOfLife.getRows(); ++r)
                for (int c = 0; c < mGameOfLife.getCols(); ++c)
                    if (mGameOfLife.getGrid()[r][c] != mGrid[r][c])
                        return true;
            return false;
        }

    }

    public UndoManager(GameOfLife gameOfLife) {
        mGameOfLife = gameOfLife;
    }

    public void pushState() {
        final int length = mUndoStack.size();
        if (length == 0 || mUndoStack.get(length - 1).stateChanged())
            mUndoStack.add(new GameOfLifeState());
    }

    public void popState() {
        GameOfLifeState state = mUndoStack.remove(mUndoStack.size() - 1);
        state.restore();
    }

    public boolean canUndo() {
        return mUndoStack.isEmpty() == false;
    }

}
