package be.ppareit.gameoflife

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import android.widget.LinearLayout
import androidx.drawerlayout.widget.DrawerLayout

class GameOfLifeActivity : Activity() {
    private lateinit var gameOfLifeView: GameOfLifeView
    private var startMenu: MenuItem? = null
    private var pauseMenu: MenuItem? = null
    private var undoMenu: MenuItem? = null
    private var singleStepMenu: MenuItem? = null
    private var controlMenu: MenuItem? = null
    private var editMenu: MenuItem? = null
    private var moveMenu: MenuItem? = null
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var drawerContainer: LinearLayout
    private lateinit var drawerListView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        gameOfLifeView = findViewById(R.id.gameoflife_view)
        drawerLayout = findViewById(R.id.drawer_layout)
        drawerContainer = findViewById(R.id.left_drawer_container)
        drawerListView = findViewById(R.id.left_drawer)
        drawerListView.adapter = DrawerListAdapter(this)
        drawerListView.setOnItemClickListener { _, _, _, id -> onDrawerItemSelected(id.toInt()) }
        drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerOpened(drawerView: View) {
                invalidateOptionsMenu()
            }

            override fun onDrawerClosed(drawerView: View) {
                invalidateOptionsMenu()
            }
        })
        actionBar?.setHomeButtonEnabled(true)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        intent?.data?.let { gameOfLifeView.doLoad(it) }
        drawerLayout.openDrawer(drawerContainer)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        startMenu = menu.findItem(R.id.start)
        pauseMenu = menu.findItem(R.id.pause)
        undoMenu = menu.findItem(R.id.undo)
        singleStepMenu = menu.findItem(R.id.single_step)
        controlMenu = menu.findItem(R.id.control_mode)
        editMenu = menu.findItem(R.id.edit)
        moveMenu = menu.findItem(R.id.move)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        if (drawerLayout.isDrawerOpen(drawerContainer)) {
            for (index in 0 until menu.size()) {
                menu.getItem(index).isVisible = false
            }
        } else {
            for (index in 0 until menu.size()) {
                menu.getItem(index).isVisible = true
            }
            when (gameOfLifeView.getGameState()) {
                GameOfLifeView.State.RUNNING -> {
                    startMenu?.isVisible = false
                    singleStepMenu?.isVisible = false
                    pauseMenu?.isVisible = true
                    undoMenu?.isVisible = false
                    controlMenu?.isVisible = false
                }
                GameOfLifeView.State.MOVING -> {
                    startMenu?.isVisible = true
                    singleStepMenu?.isVisible = true
                    pauseMenu?.isVisible = false
                    undoMenu?.isVisible = true
                    controlMenu?.isVisible = true
                    moveMenu?.isChecked = true
                    moveMenu?.icon?.let { controlMenu?.icon = it }
                }
                GameOfLifeView.State.EDITING -> {
                    startMenu?.isVisible = true
                    singleStepMenu?.isVisible = true
                    pauseMenu?.isVisible = false
                    undoMenu?.isVisible = true
                    controlMenu?.isVisible = true
                    editMenu?.isChecked = true
                    editMenu?.icon?.let { controlMenu?.icon = it }
                }
            }
            undoMenu?.isEnabled = gameOfLifeView.canUndo()
        }
        return super.onPrepareOptionsMenu(menu)
    }

    private fun onDrawerItemSelected(id: Int) {
        when (id) {
            R.id.clear -> {
                pauseGame()
                gameOfLifeView.clearGrid()
            }
            R.id.load_seed -> {
                pauseGame()
                startActivityForResult(Intent(this, LoadSeedActivity::class.java), REQUEST_LOAD_SEED)
            }
            R.id.load_from_file -> {
                pauseGame()
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                }
                startActivityForResult(intent, REQUEST_LOAD_FROM_FILE)
            }
            R.id.save_to_file -> {
                pauseGame()
                startActivityForResult(Intent(this, SaveToFileActivity::class.java), REQUEST_SAVE_TO_FILE)
            }
            R.id.settings -> startActivity(Intent(this, PreferencesActivity::class.java))
            R.id.about -> startActivity(Intent(this, AboutActivity::class.java))
            R.id.donate -> startActivity(Intent(this, DonateActivity::class.java))
        }
        drawerLayout.closeDrawer(drawerContainer)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(drawerContainer)) {
                drawerLayout.closeDrawer(drawerContainer)
            } else {
                drawerLayout.openDrawer(drawerContainer)
            }
            return true
        }

        when (item.itemId) {
            R.id.start -> gameOfLifeView.setMode(GameOfLifeView.State.RUNNING)
            R.id.pause -> pauseGame()
            R.id.undo -> gameOfLifeView.doUndo()
            R.id.single_step -> gameOfLifeView.doSingleStep()
            R.id.edit -> gameOfLifeView.setMode(GameOfLifeView.State.EDITING)
            R.id.move -> gameOfLifeView.setMode(GameOfLifeView.State.MOVING)
            else -> return super.onOptionsItemSelected(item)
        }
        invalidateOptionsMenu()
        return true
    }

    private fun pauseGame() {
        gameOfLifeView.setMode(GameOfLifeView.State.MOVING)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK || data == null) return
        when (requestCode) {
            REQUEST_LOAD_SEED -> {
                val path = data.getStringExtra("seed") ?: return
                assets.open(path).use { gameOfLifeView.doLoad(it) }
            }
            REQUEST_LOAD_FROM_FILE -> data.data?.let { gameOfLifeView.doLoad(it) }
            REQUEST_SAVE_TO_FILE -> data.data?.let { gameOfLifeView.doSave(it) }
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(drawerContainer)) {
            drawerLayout.closeDrawer(drawerContainer)
        } else {
            super.onBackPressed()
        }
    }

    override fun onPause() {
        super.onPause()
        pauseGame()
        invalidateOptionsMenu()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        undoMenu?.isEnabled = gameOfLifeView.canUndo()
    }

    companion object {
        private const val REQUEST_LOAD_FROM_FILE = 0x0001
        private const val REQUEST_LOAD_SEED = 0x0002
        private const val REQUEST_SAVE_TO_FILE = 0x0004
    }
}
