package be.ppareit.gameoflife

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameOfLifeScreen(initialIntent: Intent?) {
    val activity = LocalContext.current as ComponentActivity
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var gameView by remember { mutableStateOf<GameOfLifeView?>(null) }
    var mode by remember { mutableStateOf(GameOfLifeView.State.MOVING) }
    var canUndo by remember { mutableStateOf(false) }
    var activeDialog by remember { mutableStateOf<ActiveDialog?>(null) }
    val seeds = remember { activity.assets.list("life106")?.sorted().orEmpty() }

    val loadFromFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let { gameView?.doLoad(it) }
    }

    fun moveMode() {
        gameView?.setMode(GameOfLifeView.State.MOVING)
        mode = GameOfLifeView.State.MOVING
    }

    LaunchedEffect(Unit) {
        drawerState.open()
    }

    LaunchedEffect(gameView, initialIntent?.data) {
        val uri = initialIntent?.data
        if (gameView != null && uri != null) {
            gameView?.doLoad(uri)
        }
    }

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF222222),
            surface = Color(0xFFFAFAFA),
            background = Color(0xFF808080),
        ),
    ) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                GameDrawer(
                    onNew = {
                        moveMode()
                        gameView?.clearGrid()
                        scope.launch { drawerState.close() }
                    },
                    onLoadSeed = {
                        moveMode()
                        activeDialog = ActiveDialog.SeedPicker
                        scope.launch { drawerState.close() }
                    },
                    onLoadFromFile = {
                        moveMode()
                        loadFromFileLauncher.launch(arrayOf("*/*"))
                        scope.launch { drawerState.close() }
                    },
                    onSaveToFile = {
                        moveMode()
                        activeDialog = ActiveDialog.SaveToFile
                        scope.launch { drawerState.close() }
                    },
                    onSettings = {
                        activeDialog = ActiveDialog.Settings
                        scope.launch { drawerState.close() }
                    },
                    onAbout = {
                        activeDialog = ActiveDialog.About
                        scope.launch { drawerState.close() }
                    },
                )
            },
        ) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {},
                        navigationIcon = {
                            TextButton(onClick = { scope.launch { drawerState.open() } }) {
                                Text(stringResource(R.string.menu_label))
                            }
                        },
                        actions = {
                            GameControls(
                                mode = mode,
                                canUndo = canUndo,
                                onStart = {
                                    gameView?.setMode(GameOfLifeView.State.RUNNING)
                                    mode = GameOfLifeView.State.RUNNING
                                },
                                onPause = { moveMode() },
                                onStep = { gameView?.doSingleStep() },
                                onUndo = { gameView?.doUndo() },
                                onMove = { moveMode() },
                                onEdit = {
                                    gameView?.setMode(GameOfLifeView.State.EDITING)
                                    mode = GameOfLifeView.State.EDITING
                                },
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color(0xFF222222),
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White,
                            actionIconContentColor = Color.White,
                        ),
                    )
                },
            ) { padding ->
                AndroidView(
                    factory = { context ->
                        GameOfLifeView(context, null).also { view ->
                            gameView = view
                            view.onUndoStateChanged = { canUndo = it }
                            canUndo = view.canUndo()
                            view.setMode(mode)
                        }
                    },
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    update = { view ->
                        view.onUndoStateChanged = { canUndo = it }
                        if (view.getGameState() != mode) {
                            view.setMode(mode)
                        }
                    },
                )
            }
        }

        when (activeDialog) {
            ActiveDialog.About -> AboutDialog(onDismiss = { activeDialog = null })
            ActiveDialog.SeedPicker -> SeedPickerDialog(
                seeds = seeds,
                onDismiss = { activeDialog = null },
                onSeedSelected = { seed ->
                    activeDialog = null
                    activity.assets.open("life106/$seed").use { gameView?.doLoad(it) }
                },
            )
            ActiveDialog.SaveToFile -> SaveToFileDialog(
                onDismiss = { activeDialog = null },
                onSave = { uri ->
                    activeDialog = null
                    gameView?.doSave(uri)
                },
            )
            ActiveDialog.Settings -> SettingsDialog(onDismiss = { activeDialog = null })
            null -> Unit
        }
    }
}

private enum class ActiveDialog {
    About,
    SeedPicker,
    SaveToFile,
    Settings,
}
