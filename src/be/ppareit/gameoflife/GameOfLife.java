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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import android.graphics.Point;
import android.util.Log;

/**
 * The data and algorithm for the game of life.
 *
 * Currently a very simple algorithm is implemented. A good introduction to more advanced
 * algorithms is in "Data Structures & Program Design in C" by R. Kruse, C.L. Tondo and B.
 * Leung.
 *
 */
public class GameOfLife {

    static final String TAG = GameOfLife.class.getSimpleName();

    private int[][] mGrid;
    private int mRows;
    private int mCols;

    private int mMinimum = 2;
    private int mMaximum = 3;
    private int mSpawn = 3;

    public GameOfLife(int rows, int cols) {
        mRows = rows;
        mCols = cols;
        mGrid = new int[mRows][mCols];
        resetGrid();
    }

    public int[][] getGrid() {
        return mGrid;
    }

    public void resetGrid() {
        for (int h = 0; h < mRows; ++h)
            for (int w = 0; w < mCols; ++w)
                mGrid[h][w] = 0;
    }

    class FormatNotSupportedException extends RuntimeException {
        private static final long serialVersionUID = 3294986520736594117L;
    }

    public void loadGridFromFile(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        Set<Point> points = new HashSet<Point>();
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;

        try {
            String line = reader.readLine();
            if (!line.equals("#Life 1.06")) {
                throw new FormatNotSupportedException();
            }
            while ((line = reader.readLine()) != null) {
                String[] coords = line.split("\\s+");
                int x = Integer.parseInt(coords[0]);
                int y = Integer.parseInt(coords[1]);

                points.add(new Point(x, y));

                minX = Math.min(x, minX);
                maxX = Math.max(x, maxX);
                minY = Math.min(y, minY);
                maxY = Math.max(y, maxY);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        points = offset(points, -minX, -minY);
        maxX -= minX;
        maxY -= minY;

        loadGrid(points, maxX, maxY);
    }

    private void loadGrid(Set<Point> points, int maxX, int maxY) {
        resetGrid();

        final int rowOffset = (getRows() - maxY) / 2;
        final int colOffset = (getCols() - maxX) / 2;

        for (Point p : points) {
            if ((p.y + rowOffset < 0) || (p.x + colOffset < 0)
                    || (p.y + rowOffset >= getRows()) || (p.x + colOffset >= getCols()))
                continue;
            mGrid[rowOffset + p.y][colOffset + p.x] = 1;
        }
    }

    static private Set<Point> offset(Set<Point> points, int dx, int dy) {
        try {
            @SuppressWarnings("unchecked")
            Set<Point> fixedPoints = points.getClass().newInstance();
            for (Point p : points) {
                p.offset(dx, dy);
                fixedPoints.add(p);
            }
            return fixedPoints;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void generateNextGeneration() {
        int neighbors;

        int[][] nextGenerationGrid = new int[mRows][mCols];

        for (int h = 0; h < mRows; ++h) {
            for (int w = 0; w < mCols; ++w) {
                neighbors = calculateNeighbors(h, w);

                if (mGrid[h][w] != 0) {
                    if ((neighbors >= mMinimum) && (neighbors <= mMaximum)) {
                        nextGenerationGrid[h][w] = neighbors;
                    }
                } else {
                    if (neighbors == mSpawn) {
                        nextGenerationGrid[h][w] = mSpawn;
                    }
                }
            }
        }

        copyGrid(nextGenerationGrid);

    }

    private void copyGrid(int[][] nextGenerationGrid) {
        for (int h = 0; h < mRows; ++h)
            for (int w = 0; w < mCols; ++w)
                mGrid[h][w] = nextGenerationGrid[h][w];
    }

    private int calculateNeighbors(int y, int x) {
        int total = (mGrid[y][x] != 0) ? -1 : 0;
        for (int h = -1; h <= +1; ++h)
            for (int w = -1; w <= +1; ++w)
                if (mGrid[(mRows + y + h) % mRows][(mCols + x + w) % mCols] != 0)
                    total++;
        return total;
    }

    public int getRows() {
        return mRows;
    }

    public int getCols() {
        return mCols;
    }

    public void setUnderPopulation(int minimumVariable) {
        Log.d(TAG, "Setting underpopulation to: " + minimumVariable);
        mMinimum = minimumVariable;
    }

    public void setOverPopulation(int maximumVariable) {
        Log.d(TAG, "Setting overpopulation to: " + maximumVariable);
        mMaximum = maximumVariable;
    }

    public void setSpawn(int spawnVariable) {
        Log.d(TAG, "Setting spawnvariable to: " + spawnVariable);
        mSpawn = spawnVariable;
    }

}
