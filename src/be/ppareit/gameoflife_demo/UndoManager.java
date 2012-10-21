package be.ppareit.gameoflife_demo;

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
