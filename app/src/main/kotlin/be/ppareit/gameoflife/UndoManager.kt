package be.ppareit.gameoflife

class UndoManager(private val gameOfLife: GameOfLife) {
    private val undoStack = mutableListOf<GameOfLifeState>()

    private inner class GameOfLifeState {
        private val grid = Array(gameOfLife.getRows()) { row ->
            gameOfLife.grid[row].copyOf()
        }

        fun restore() {
            for (row in 0 until gameOfLife.getRows()) {
                for (col in 0 until gameOfLife.getCols()) {
                    gameOfLife.grid[row][col] = grid[row][col]
                }
            }
        }

        fun stateChanged(): Boolean {
            for (row in 0 until gameOfLife.getRows()) {
                for (col in 0 until gameOfLife.getCols()) {
                    if (gameOfLife.grid[row][col] != grid[row][col]) return true
                }
            }
            return false
        }
    }

    fun pushState() {
        if (undoStack.isEmpty() || undoStack.last().stateChanged()) {
            undoStack.add(GameOfLifeState())
        }
    }

    fun popState() {
        undoStack.removeAt(undoStack.lastIndex).restore()
    }

    fun canUndo(): Boolean = undoStack.isNotEmpty()
}
