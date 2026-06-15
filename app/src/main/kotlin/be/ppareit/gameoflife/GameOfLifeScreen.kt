package be.ppareit.gameoflife

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameOfLifeScreen(initialUri: Uri?) {
    val context = LocalContext.current
    val assets = context.assets
    val session: GameSessionViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle<GameSettings?>(initialValue = null)
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var gameView by remember { mutableStateOf<GameOfLifeView?>(null) }
    var activeDialog by remember { mutableStateOf<ActiveDialog?>(null) }
    val seeds = remember(assets) { assets.list("life106")?.sorted().orEmpty() }
    val mode = session.mode
    val canUndo = session.canUndo

    val loadFromFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let {
            showFileOperationResult(context, session.load(it))
            gameView?.invalidateBoard()
        }
    }

    val saveToFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream"),
    ) { uri ->
        uri?.let {
            showFileOperationResult(context, session.save(it))
        }
    }

    LaunchedEffect(Unit) {
        drawerState.open()
    }

    LaunchedEffect(gameView, initialUri) {
        if (gameView != null && initialUri != null) {
            showFileOperationResult(context, session.load(initialUri))
            gameView?.invalidateBoard()
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
            gesturesEnabled = drawerState.isOpen,
            drawerContent = {
                GameDrawer(
                    onNew = {
                        session.changeMode(GameMode.MOVING)
                        session.clearGrid()
                        gameView?.invalidateBoard()
                        scope.launch { drawerState.close() }
                    },
                    onLoadSeed = {
                        session.changeMode(GameMode.MOVING)
                        activeDialog = ActiveDialog.SeedPicker
                        scope.launch { drawerState.close() }
                    },
                    onLoadFromFile = {
                        session.changeMode(GameMode.MOVING)
                        loadFromFileLauncher.launch(arrayOf("*/*"))
                        scope.launch { drawerState.close() }
                    },
                    onSaveToFile = {
                        session.changeMode(GameMode.MOVING)
                        saveToFileLauncher.launch("game-of-life.life")
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
                                    session.changeMode(GameMode.RUNNING)
                                },
                                onPause = {
                                    session.changeMode(GameMode.MOVING)
                                },
                                onStep = {
                                    session.step()
                                    gameView?.invalidateBoard()
                                },
                                onUndo = {
                                    session.undo()
                                    gameView?.invalidateBoard()
                                },
                                onMove = {
                                    session.changeMode(GameMode.MOVING)
                                },
                                onEdit = {
                                    session.changeMode(GameMode.EDITING)
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
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                ) {
                    settings?.let { currentSettings ->
                        AndroidView(
                            factory = { context ->
                                GameOfLifeView(context, null, currentSettings).also { view ->
                                    gameView = view
                                    view.session = session
                                    view.applySettings(currentSettings)
                                    view.applyMode(mode)
                                }
                            },
                            modifier = Modifier.fillMaxSize(),
                            update = { view ->
                                view.applySettings(currentSettings)
                                view.session = session
                                view.applyMode(mode)
                            },
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(24.dp)
                            .pointerInput(drawerState.isClosed) {
                                if (!drawerState.isClosed) return@pointerInput

                                var dragDistance = 0f
                                detectHorizontalDragGestures(
                                    onDragStart = { dragDistance = 0f },
                                    onHorizontalDrag = { change, dragAmount ->
                                        if (dragAmount > 0) {
                                            dragDistance += dragAmount
                                            change.consume()
                                        }
                                    },
                                    onDragEnd = {
                                        if (dragDistance > 48f) {
                                            scope.launch { drawerState.open() }
                                        }
                                    },
                                )
                            },
                    )
                }
            }
        }

        when (activeDialog) {
            ActiveDialog.About -> AboutDialog(onDismiss = { activeDialog = null })
            ActiveDialog.SeedPicker -> SeedPickerDialog(
                seeds = seeds,
                onDismiss = { activeDialog = null },
                onSeedSelected = { seed ->
                    activeDialog = null
                    assets.open("life106/$seed").use { session.load(it) }
                    gameView?.invalidateBoard()
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
    Settings,
}

private fun showFileOperationResult(context: Context, result: FileOperationResult) {
    when (result) {
        FileOperationResult.Success -> Unit
        FileOperationResult.FileNotFound -> {
            Toast.makeText(context, R.string.file_not_found, Toast.LENGTH_SHORT).show()
        }
        FileOperationResult.FormatNotSupported -> {
            Toast.makeText(context, R.string.file_format_not_supported, Toast.LENGTH_LONG).show()
        }
    }
}
