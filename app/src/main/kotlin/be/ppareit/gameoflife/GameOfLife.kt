package be.ppareit.gameoflife

import android.util.Log
import be.ppareit.gameoflife.patterns.GridPoint
import be.ppareit.gameoflife.patterns.Life106Format
import be.ppareit.gameoflife.patterns.PatternFormats
import java.io.InputStream
import java.io.OutputStream
import kotlin.random.Random

class GameOfLife(private val rows: Int, private val cols: Int) {
    private var minimum = 2
    private var maximum = 3
    private var spawn = 3
    val grid: Array<IntArray> = Array(rows) { IntArray(cols) }

    init {
        resetGrid()
    }

    fun resetGrid() {
        for (row in 0 until rows) {
            grid[row].fill(0)
        }
    }

    fun randomizeGrid(density: Double, random: Random = Random.Default) {
        require(density in 0.0..1.0) { "density must be between 0.0 and 1.0" }

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                grid[row][col] = if (random.nextDouble() < density) 1 else 0
            }
        }
    }

    fun loadGridFromFileOnRandomBackground(
        inputStream: InputStream,
        density: Double,
        padding: Int,
        random: Random = Random.Default,
    ) {
        require(padding >= 0) { "padding must be non-negative" }

        val points = PatternFormats.read(inputStream).cells
        randomizeGrid(density, random)

        if (points.isEmpty()) return

        val placement = centeredPlacement(points)
        clearArea(
            minRow = placement.rowOffset - padding,
            minCol = placement.colOffset - padding,
            maxRow = placement.rowOffset + placement.maxY + padding,
            maxCol = placement.colOffset + placement.maxX + padding,
        )
        placeGrid(placement.points, placement.rowOffset, placement.colOffset)
    }

    fun loadGridFromFile(inputStream: InputStream) {
        val points = PatternFormats.read(inputStream).cells

        if (points.isEmpty()) {
            resetGrid()
            return
        }

        val placement = centeredPlacement(points)
        resetGrid()
        placeGrid(placement.points, placement.rowOffset, placement.colOffset)
    }

    private fun placeGrid(points: Set<GridPoint>, rowOffset: Int, colOffset: Int) {
        for (point in points) {
            val row = rowOffset + point.y
            val col = colOffset + point.x
            if (row !in 0 until getRows() || col !in 0 until getCols()) continue
            grid[row][col] = 1
        }
    }

    private fun centeredPlacement(points: Set<GridPoint>): GridPlacement {
        val minX = points.minOf { it.x }
        val maxX = points.maxOf { it.x }
        val minY = points.minOf { it.y }
        val maxY = points.maxOf { it.y }
        val shifted = offset(points, -minX, -minY)
        val normalizedMaxX = maxX - minX
        val normalizedMaxY = maxY - minY

        return GridPlacement(
            points = shifted,
            rowOffset = (getRows() - normalizedMaxY) / 2,
            colOffset = (getCols() - normalizedMaxX) / 2,
            maxX = normalizedMaxX,
            maxY = normalizedMaxY,
        )
    }

    private fun clearArea(minRow: Int, minCol: Int, maxRow: Int, maxCol: Int) {
        for (row in minRow.coerceAtLeast(0)..maxRow.coerceAtMost(rows - 1)) {
            for (col in minCol.coerceAtLeast(0)..maxCol.coerceAtMost(cols - 1)) {
                grid[row][col] = 0
            }
        }
    }

    fun generateNextGeneration() {
        val nextGenerationGrid = Array(rows) { IntArray(cols) }
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val neighbors = calculateNeighbors(row, col)
                if (grid[row][col] != 0) {
                    if (neighbors in minimum..maximum) {
                        nextGenerationGrid[row][col] = neighbors
                    }
                } else if (neighbors == spawn) {
                    nextGenerationGrid[row][col] = spawn
                }
            }
        }
        copyGrid(nextGenerationGrid)
    }

    private fun copyGrid(nextGenerationGrid: Array<IntArray>) {
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                grid[row][col] = nextGenerationGrid[row][col]
            }
        }
    }

    private fun calculateNeighbors(y: Int, x: Int): Int {
        var total = if (grid[y][x] != 0) -1 else 0
        for (rowOffset in -1..1) {
            for (colOffset in -1..1) {
                if (grid[(rows + y + rowOffset) % rows][(cols + x + colOffset) % cols] != 0) {
                    total++
                }
            }
        }
        return total
    }

    fun getRows(): Int = rows
    fun getCols(): Int = cols

    fun setUnderPopulation(minimumVariable: Int) {
        Log.d(TAG, "Setting underpopulation to: $minimumVariable")
        minimum = minimumVariable
    }

    fun setOverPopulation(maximumVariable: Int) {
        Log.d(TAG, "Setting overpopulation to: $maximumVariable")
        maximum = maximumVariable
    }

    fun setSpawn(spawnVariable: Int) {
        Log.d(TAG, "Setting spawnvariable to: $spawnVariable")
        spawn = spawnVariable
    }

    fun saveGridToFile(outputStream: OutputStream) {
        val points = mutableSetOf<GridPoint>()
        var minCol = cols
        var minRow = rows

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                if (grid[row][col] != 0) {
                    points.add(GridPoint(col, row))
                    minCol = minOf(minCol, col)
                    minRow = minOf(minRow, row)
                }
            }
        }

        val shifted = if (points.isEmpty()) points else offset(points, -minCol, -minRow)
        Life106Format.write(shifted, outputStream)
    }

    companion object {
        private val TAG = GameOfLife::class.java.simpleName

        private fun offset(points: Set<GridPoint>, dx: Int, dy: Int): MutableSet<GridPoint> {
            return points.mapTo(mutableSetOf()) { point ->
                GridPoint(point.x + dx, point.y + dy)
            }
        }
    }
}

private data class GridPlacement(
    val points: Set<GridPoint>,
    val rowOffset: Int,
    val colOffset: Int,
    val maxX: Int,
    val maxY: Int,
)
