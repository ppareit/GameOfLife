package be.ppareit.gameoflife

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import be.ppareit.gameoflife.patterns.PatternFormatException
import java.io.FileNotFoundException
import java.io.InputStream

class GameSessionViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val undoManager: UndoManager

    val game: GameOfLife
    var mode by mutableStateOf(GameMode.MOVING)
        private set
    var canUndo by mutableStateOf(false)
        private set

    init {
        val settings = GameSettings()
        game = GameOfLife(settings.rows, settings.cols).also {
            it.setUnderPopulation(settings.minimumVariable)
            it.setOverPopulation(settings.maximumVariable)
            it.setSpawn(settings.spawnVariable)
            appContext.resources.openRawResource(R.raw.android).use { input ->
                it.loadGridFromFileOnRandomBackground(
                    inputStream = input,
                    density = RANDOM_SEED_DENSITY,
                    padding = STARTUP_PATTERN_PADDING,
                )
            }
        }
        undoManager = UndoManager(game)
    }

    fun changeMode(newMode: GameMode) {
        if (newMode == GameMode.RUNNING && mode != GameMode.RUNNING) {
            pushUndoState()
        }
        mode = newMode
    }

    fun clearGrid() {
        pushUndoState()
        game.resetGrid()
    }

    fun randomizeGrid() {
        pushUndoState()
        game.randomizeGrid(RANDOM_SEED_DENSITY)
    }

    fun step() {
        pushUndoState()
        game.generateNextGeneration()
    }

    fun undo() {
        if (!undoManager.canUndo()) return
        undoManager.popState()
        updateUndoState()
    }

    fun load(inputStream: InputStream) {
        game.loadGridFromFile(inputStream)
        updateUndoState()
    }

    fun load(uri: Uri): FileOperationResult {
        return try {
            appContext.contentResolver.openInputStream(uri).use { input ->
                if (input == null) throw FileNotFoundException()
                load(input)
            }
            FileOperationResult.Success
        } catch (_: FileNotFoundException) {
            FileOperationResult.FileNotFound
        } catch (_: PatternFormatException) {
            FileOperationResult.FormatNotSupported
        }
    }

    fun save(uri: Uri): FileOperationResult {
        return try {
            appContext.contentResolver.openOutputStream(uri).use { output ->
                if (output == null) throw FileNotFoundException()
                game.saveGridToFile(output)
            }
            FileOperationResult.Success
        } catch (_: FileNotFoundException) {
            FileOperationResult.FileNotFound
        }
    }

    fun pushUndoState() {
        undoManager.pushState()
        updateUndoState()
    }

    private fun updateUndoState() {
        canUndo = undoManager.canUndo()
    }
}

enum class FileOperationResult {
    Success,
    FileNotFound,
    FormatNotSupported,
}

enum class GameMode {
    RUNNING,
    EDITING,
    MOVING,
}

private const val RANDOM_SEED_DENSITY = 0.25
private const val STARTUP_PATTERN_PADDING = 2
