package be.ppareit.gameoflife

import android.graphics.Point
import android.util.Log
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.PrintWriter

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

    class FormatNotSupportedException : RuntimeException()

    fun loadGridFromFile(inputStream: InputStream) {
        val points = mutableSetOf<Point>()
        var minX = Int.MAX_VALUE
        var maxX = Int.MIN_VALUE
        var minY = Int.MAX_VALUE
        var maxY = Int.MIN_VALUE

        BufferedReader(InputStreamReader(inputStream)).use { reader ->
            val firstLine = reader.readLine()
            if (firstLine != "#Life 1.06") {
                throw FormatNotSupportedException()
            }

            reader.lineSequence().forEach { line ->
                if (line.isBlank()) return@forEach
                val coords = line.trim().split(Regex("\\s+"))
                val x = coords[0].toInt()
                val y = coords[1].toInt()
                points.add(Point(x, y))
                minX = minOf(x, minX)
                maxX = maxOf(x, maxX)
                minY = minOf(y, minY)
                maxY = maxOf(y, maxY)
            }
        }

        if (points.isEmpty()) {
            resetGrid()
            return
        }

        val shifted = offset(points, -minX, -minY)
        loadGrid(shifted, maxX - minX, maxY - minY)
    }

    private fun loadGrid(points: Set<Point>, maxX: Int, maxY: Int) {
        resetGrid()
        val rowOffset = (getRows() - maxY) / 2
        val colOffset = (getCols() - maxX) / 2

        for (point in points) {
            val row = rowOffset + point.y
            val col = colOffset + point.x
            if (row !in 0 until getRows() || col !in 0 until getCols()) continue
            grid[row][col] = 1
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
        val points = mutableSetOf<Point>()
        var minCol = cols
        var minRow = rows

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                if (grid[row][col] != 0) {
                    points.add(Point(col, row))
                    minCol = minOf(minCol, col)
                    minRow = minOf(minRow, row)
                }
            }
        }

        val shifted = if (points.isEmpty()) points else offset(points, -minCol, -minRow)
        PrintWriter(outputStream).use { writer ->
            writer.println("#Life 1.06")
            for (point in shifted) {
                writer.println("${point.x} ${point.y}")
            }
        }
    }

    companion object {
        private val TAG = GameOfLife::class.java.simpleName

        private fun offset(points: Set<Point>, dx: Int, dy: Int): MutableSet<Point> {
            return points.mapTo(mutableSetOf()) { point ->
                Point(point.x + dx, point.y + dy)
            }
        }
    }
}
