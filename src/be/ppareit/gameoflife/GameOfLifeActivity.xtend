/*******************************************************************************
 * Copyright (c) 2011-2013 Pieter Pareit.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Pieter Pareit - initial API and implementation
 ******************************************************************************/
package be.ppareit.gameoflife

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.ipaulpro.afilechooser.FileChooserActivity
import org.xtendroid.annotations.AddLogTag

import be.ppareit.gameoflife.R
import android.widget.ListView

import static extension be.ppareit.android.AndroidUtils.*
import android.widget.ArrayAdapter
import android.support.v4.app.ActionBarDrawerToggle
import android.support.v4.widget.DrawerLayout
import android.view.View

/**
 * Main activity for the Game of Life.
 *
 */
@AddLogTag
public class GameOfLifeActivity extends Activity {

    private static final int REQUEST_CHOOSER = 0x0001

    private GameOfLifeView mGameOfLifeView

    private MenuItem mStartMenu
    private MenuItem mPauseMenu
    private MenuItem mUndoMenu
    private MenuItem mSingleStepMenu
    private MenuItem mControlMenu
    private MenuItem mEditMenu
    private MenuItem mMoveMenu

    private DrawerLayout mDrawerLayout
    private ActionBarDrawerToggle mDrawerToggle
    private ListView mDrawerListView

    override onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        mGameOfLifeView = findView(R.id.gameoflife_view)

        mDrawerLayout = findView(R.id.drawer_layout)
        mDrawerListView = findView(R.id.left_drawer)
        mDrawerListView.adapter = new DrawerListAdapter(this)
        mDrawerListView.onItemClickListener = [ parent, view, position, id |
            onDrawerItemSelected(id as int);
        ]
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
            R.drawable.launcher, R.string.app_name, R.string.app_name) {
            override onDrawerClosed(View view) {
                Log.d(TAG, "Closed drawer")
                invalidateOptionsMenu()
            }

            override onDrawerOpened(View view) {
                Log.d(TAG, "Opened drawer")
                invalidateOptionsMenu()
            }
        }
        mDrawerLayout.drawerListener = mDrawerToggle

        actionBar.homeButtonEnabled = true

        val intent = getIntent()
        if(intent != null) {
            Log.v(TAG, "Activity started using intent")
            val uri = intent.getData()
            if(uri != null) {
                Log.v(TAG, "Uri received: " + uri.toString())
                mGameOfLifeView.doLoad(uri)
            }
        }

        // opening the app should initially show the drawer
        // @TODO: once the user has opened drawer once, this should no longer open at start
        mDrawerLayout.openDrawer(mDrawerListView)

    }

    override onCreateOptionsMenu(Menu menu) {
        val inflater = getMenuInflater()
        inflater.inflate(R.menu.main, menu)

        mStartMenu = menu.findItem(R.id.start)
        mPauseMenu = menu.findItem(R.id.pause)
        mUndoMenu = menu.findItem(R.id.undo)
        mSingleStepMenu = menu.findItem(R.id.single_step)
        mControlMenu = menu.findItem(R.id.control_mode)
        mEditMenu = menu.findItem(R.id.edit)
        mMoveMenu = menu.findItem(R.id.move)

        return true
    }

    override onPrepareOptionsMenu(Menu menu) {
        val drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerListView)

        if (drawerOpen == true) {
            // hide all menus when drawer is open
            for (i : 0 .. menu.size - 1) {
                var item = menu.getItem(i);
                item.visible = false
            }

        } else {
            // else, look at the gamestate
            switch (mGameOfLifeView.gameState) {
                case GameOfLifeView.State.RUNNING: {
                    mStartMenu.visible = false
                    mSingleStepMenu.visible = false
                    mPauseMenu.visible = true
                    mPauseMenu.enabled = true
                    mUndoMenu.visible = false
                    mControlMenu.visible = false
                }
                case GameOfLifeView.State.MOVING: {
                    mStartMenu.visible = true
                    mSingleStepMenu.visible = true
                    mPauseMenu.visible = false
                    mUndoMenu.visible = true
                    mControlMenu.visible = true
                    mControlMenu.icon = mMoveMenu.icon
                    mMoveMenu.checked = true
                }
                case GameOfLifeView.State.EDITING: {
                    mStartMenu.visible = true
                    mSingleStepMenu.visible = true
                    mPauseMenu.visible = false
                    mUndoMenu.visible = true
                    mControlMenu.visible = true
                    mControlMenu.icon = mEditMenu.icon
                    mEditMenu.checked = true
                }
            }
            mUndoMenu.enabled = mGameOfLifeView.canUndo
        }

        return super.onPrepareOptionsMenu(menu)
    }

    def onDrawerItemSelected(int id) {
        switch (id) {
            case R.id.clear: {
                pauseGame()
                mGameOfLifeView.clearGrid()
            }
            case R.id.load_from_file: {
                pauseGame()
                var intent = new Intent(this, FileChooserActivity)
                startActivityForResult(intent, REQUEST_CHOOSER)
            }
            case R.id.settings: {
                startActivity(new Intent(this, PreferencesActivity))
            }
            case R.id.about: {
                startActivity(new Intent(this, AboutActivity))
            }
        }
        mDrawerLayout.closeDrawer(mDrawerListView)
    }

    override onOptionsItemSelected(MenuItem item) {

        // Pressing home/up will show/hide the navigation drawer
        if(mDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }

        switch item.getItemId() {
            case R.id.start: {
                mGameOfLifeView.mode = GameOfLifeView.State.RUNNING
            }
            case R.id.pause: {
                pauseGame()
            }
            case R.id.undo: {
                mGameOfLifeView.doUndo()
            }
            case R.id.single_step: {
                mGameOfLifeView.doSingleStep()
            }
            case R.id.edit: {
                mGameOfLifeView.mode = GameOfLifeView.State.EDITING
            }
            case R.id.move: {
                mGameOfLifeView.mode = GameOfLifeView.State.MOVING
            }
            default:
                return super.onOptionsItemSelected(item)
        }

        invalidateOptionsMenu()
        return true;
    }

    def pauseGame() {
        mGameOfLifeView.mode = GameOfLifeView.State.MOVING
    }

    override onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHOOSER:
                if(resultCode == RESULT_OK) {
                    val uri = data.getData()
                    mGameOfLifeView.doLoad(uri)
                }
        }
    }

    override onBackPressed() {
        if(mDrawerLayout.isDrawerOpen(mDrawerListView)) {
            mDrawerLayout.closeDrawer(mDrawerListView)
        } else {
            super.onBackPressed()
        }
    }

    override onPause() {
        super.onPause()
        pauseGame()
        invalidateOptionsMenu()
    }

    override onUserInteraction() {
        super.onUserInteraction()
        mUndoMenu.enabled = mGameOfLifeView.canUndo
    }

}
