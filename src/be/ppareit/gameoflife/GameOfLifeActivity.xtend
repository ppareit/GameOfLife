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
    private MenuItem mClearMenu
    private MenuItem mControlMenu
    private MenuItem mEditMenu
    private MenuItem mMoveMenu

    private DrawerLayout mDrawerLayout
    private ActionBarDrawerToggle mDrawerToggle
    private ListView mDrawerListView
    private String[] mDrawerItems = #["Settings...", "About..."]
    private Class[] mDrawerActivities = #[PreferencesActivity, AboutActivity]

    override onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        mGameOfLifeView = findView(R.id.gameoflife_view)

        mDrawerLayout = findView(R.id.drawer_layout)
        mDrawerListView = findView(R.id.left_drawer)
        mDrawerListView.adapter = new ArrayAdapter<String>(this,
            android.R.layout.simple_list_item_1, mDrawerItems)
        mDrawerListView.onItemClickListener = [ parent, view, position, id |
            startActivity(new Intent(this, mDrawerActivities.get(position) as Class))
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

        actionBar.displayHomeAsUpEnabled = true
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
        mClearMenu = menu.findItem(R.id.clear)
        mControlMenu = menu.findItem(R.id.control_mode)
        mEditMenu = menu.findItem(R.id.edit)
        mMoveMenu = menu.findItem(R.id.move)

        mPauseMenu.setVisible(false).setEnabled(false)
        mUndoMenu.setEnabled(false)

        return true
    }
    
    override onPrepareOptionsMenu(Menu menu) {
        val drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerListView)
        for (i : 0..menu.size-1) {
            var item = menu.getItem(i);
            item.visible = !drawerOpen
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override onOptionsItemSelected(MenuItem item) {

        // Pressing home/up will show/hide the navigation drawer
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }

        switch item.getItemId() {
            case R.id.load: {
                pauseGame()
                var intent = new Intent(this, FileChooserActivity)
                startActivityForResult(intent, REQUEST_CHOOSER)
                return true
            }
            case R.id.start: {
                startGame()
                return true
            }
            case R.id.pause: {
                pauseGame()
                return true
            }
            case R.id.undo: {
                mGameOfLifeView.doUndo()
                mUndoMenu.setEnabled(mGameOfLifeView.canUndo())
                return true
            }
            case R.id.single_step: {
                mGameOfLifeView.doSingleStep()
                mUndoMenu.setEnabled(true)
                return true
            }
            case R.id.edit: {
                mControlMenu.setIcon(item.getIcon())
                mControlMenu.setTitle(item.getTitle())
                item.setChecked(true)
                updatePausedMode()
                mUndoMenu.setEnabled(mGameOfLifeView.canUndo())
                return true
            }
            case R.id.move: {
                mControlMenu.setIcon(item.getIcon())
                mControlMenu.setTitle(item.getTitle())
                item.setChecked(true)
                updatePausedMode()
                mUndoMenu.setEnabled(mGameOfLifeView.canUndo())
                return true
            }
            case R.id.clear: {
                mGameOfLifeView.clearGrid()
                mUndoMenu.setEnabled(mGameOfLifeView.canUndo())
                return true
            }
            default:
                return super.onOptionsItemSelected(item)
        }
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
        if (mDrawerLayout.isDrawerOpen(mDrawerListView)) {
            mDrawerLayout.closeDrawer(mDrawerListView)
        } else {
            super.onBackPressed()
        }
    }

    def pauseGame() {
        updatePausedMode()
        mPauseMenu.setVisible(false).setEnabled(false)
        mStartMenu.setVisible(true).setEnabled(true)
        mSingleStepMenu.setEnabled(true)
        mUndoMenu.setEnabled(true)
        mClearMenu.setEnabled(true)
        mControlMenu.setEnabled(true)
    }

    def startGame() {
        mGameOfLifeView.setMode(GameOfLifeView.State.RUNNING)
        mClearMenu.setEnabled(false)
        mStartMenu.setVisible(false).setEnabled(false)
        mPauseMenu.setVisible(true).setEnabled(true)
        mUndoMenu.setEnabled(false)
        mSingleStepMenu.setEnabled(false)
        mControlMenu.setEnabled(false)
    }

    override onPause() {
        super.onPause()
        updatePausedMode()
        mPauseMenu.setVisible(false).setEnabled(false)
        mStartMenu.setVisible(true).setEnabled(true)
    }

    def updatePausedMode() {
        if(mEditMenu.isChecked()) {
            mGameOfLifeView.setMode(GameOfLifeView.State.EDITING)
        } else if(mMoveMenu.isChecked()) {
            mGameOfLifeView.setMode(GameOfLifeView.State.MOVING)
        }
    }

    override onUserInteraction() {
        super.onUserInteraction()
        mUndoMenu.setEnabled(mGameOfLifeView.canUndo())
    }

}
