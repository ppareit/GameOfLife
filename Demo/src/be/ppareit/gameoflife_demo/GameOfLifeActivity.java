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
package be.ppareit.gameoflife_demo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.ipaulpro.afilechooser.FileChooserActivity;

/**
 * Main activity for the Game of Life.
 * 
 */
public class GameOfLifeActivity extends Activity {

    private static final String TAG = GameOfLifeActivity.class.getSimpleName();

    private static final int REQUEST_CHOOSER = 0x0001;

    private GameOfLifeView mGameOfLifeView;

    private MenuItem mStartMenu;
    private MenuItem mPauseMenu;
    private MenuItem mUndoMenu;
    private MenuItem mSingleStepMenu;
    private MenuItem mClearMenu;
    private MenuItem mControlMenu;
    private MenuItem mEditMenu;
    private MenuItem mMoveMenu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mGameOfLifeView = (GameOfLifeView) findViewById(R.id.gameoflife_view);

        final Intent intent = getIntent();
        if (intent != null) {
            Log.v(TAG, "Activity started using intent");
            final Uri uri = intent.getData();
            if (uri != null) {
                Log.v(TAG, "Uri received: " + uri.toString());
                mGameOfLifeView.doLoad(uri);
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        mStartMenu = menu.findItem(R.id.start);
        mPauseMenu = menu.findItem(R.id.pause);
        mUndoMenu = menu.findItem(R.id.undo);
        mSingleStepMenu = menu.findItem(R.id.single_step);
        mClearMenu = menu.findItem(R.id.clear);
        mControlMenu = menu.findItem(R.id.control_mode);
        mEditMenu = menu.findItem(R.id.edit);
        mMoveMenu = menu.findItem(R.id.move);

        mPauseMenu.setVisible(false).setEnabled(false);
        mUndoMenu.setEnabled(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.load:
            pauseGame();
            Intent intent = new Intent(this, FileChooserActivity.class);
            startActivityForResult(intent, REQUEST_CHOOSER);
            return true;
        case R.id.start:
            startGame();
            return true;
        case R.id.pause:
            pauseGame();
            return true;
        case R.id.undo:
            mGameOfLifeView.doUndo();
            mUndoMenu.setEnabled(mGameOfLifeView.canUndo());
            return true;
        case R.id.single_step:
            mGameOfLifeView.doSingleStep();
            mUndoMenu.setEnabled(true);
            return true;
        case R.id.edit:
        case R.id.move:
            mControlMenu.setIcon(item.getIcon());
            mControlMenu.setTitle(item.getTitle());
            item.setChecked(true);
            updatePausedMode();
            mUndoMenu.setEnabled(mGameOfLifeView.canUndo());
            return true;
        case R.id.clear:
            mGameOfLifeView.clearGrid();
            mUndoMenu.setEnabled(mGameOfLifeView.canUndo());
            return true;
        case R.id.settings:
            startActivity(new Intent(this, PreferencesActivity.class));
            return true;
        case R.id.about:
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case REQUEST_CHOOSER:
            if (resultCode == RESULT_OK) {
                final Uri uri = data.getData();
                mGameOfLifeView.doLoad(uri);
            }
        }
    }

    private void pauseGame() {
        updatePausedMode();
        mPauseMenu.setVisible(false).setEnabled(false);
        mStartMenu.setVisible(true).setEnabled(true);
        mSingleStepMenu.setEnabled(true);
        mUndoMenu.setEnabled(true);
        mClearMenu.setEnabled(true);
        mControlMenu.setEnabled(true);
    }

    private void startGame() {
        mGameOfLifeView.setMode(GameOfLifeView.State.RUNNING);
        mClearMenu.setEnabled(false);
        mStartMenu.setVisible(false).setEnabled(false);
        mPauseMenu.setVisible(true).setEnabled(true);
        mUndoMenu.setEnabled(false);
        mSingleStepMenu.setEnabled(false);
        mControlMenu.setEnabled(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        updatePausedMode();
        mPauseMenu.setVisible(false).setEnabled(false);
        mStartMenu.setVisible(true).setEnabled(true);
    }

    private void updatePausedMode() {
        if (mEditMenu.isChecked())
            mGameOfLifeView.setMode(GameOfLifeView.State.EDITING);
        else if (mMoveMenu.isChecked())
            mGameOfLifeView.setMode(GameOfLifeView.State.MOVING);
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        mUndoMenu.setEnabled(mGameOfLifeView.canUndo());
    }

}
