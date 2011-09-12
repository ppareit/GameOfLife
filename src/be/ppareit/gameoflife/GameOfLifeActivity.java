/*******************************************************************************
 * Copyright (c) 2011 Pieter Pareit.
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
package be.ppareit.gameoflife;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * Main activity for the Game of Life.
 * 
 */
public class GameOfLifeActivity extends Activity {

    private GameOfLifeView mGameOfLifeView;
    
    private MenuItem mStartMenu;
    private MenuItem mSingleStepMenu;
    private MenuItem mPauseMenu;
    private MenuItem mClearMenu;
    private MenuItem mControlMenu;
    private MenuItem mEditMenu;
    private MenuItem mMoveMenu;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mGameOfLifeView = (GameOfLifeView)findViewById(R.id.gameoflife_view);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        
        mStartMenu = menu.findItem(R.id.start);
        mSingleStepMenu = menu.findItem(R.id.single_step);
        mPauseMenu = menu.findItem(R.id.pause);
        mClearMenu = menu.findItem(R.id.clear);
        mControlMenu = menu.findItem(R.id.control_mode);
        mEditMenu = menu.findItem(R.id.edit);
        mMoveMenu = menu.findItem(R.id.move);
        
        mPauseMenu.setVisible(false).setEnabled(false);
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.start:
            mGameOfLifeView.setMode(GameOfLifeView.State.RUNNING);
            mClearMenu.setEnabled(false);
            mStartMenu.setVisible(false).setEnabled(false);
            mSingleStepMenu.setEnabled(false);
            mPauseMenu.setVisible(true).setEnabled(true);
            mControlMenu.setEnabled(false);
            return true;
        case R.id.single_step:
            mGameOfLifeView.doSingleStep();
            return true;
        case R.id.pause:
            updatePausedMode();
            mPauseMenu.setVisible(false).setEnabled(false);
            mStartMenu.setVisible(true).setEnabled(true);
            mSingleStepMenu.setEnabled(true);
            mClearMenu.setEnabled(true);
            mControlMenu.setEnabled(true);
            return true;
        case R.id.edit:
        case R.id.move:
            mControlMenu.setIcon(item.getIcon());
            mControlMenu.setTitle(item.getTitle());
            item.setChecked(true);
            updatePausedMode();
            return true;
        case R.id.clear:
            mGameOfLifeView.clearGrid();
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
    protected void onPause() {
        super.onPause();
        updatePausedMode();
        mPauseMenu.setVisible(false).setEnabled(false);
        mStartMenu.setVisible(true).setEnabled(true);
    }
    
    private void updatePausedMode() {
        if (mEditMenu.isChecked()) mGameOfLifeView.setMode(GameOfLifeView.State.EDITING);
        else if (mMoveMenu.isChecked()) mGameOfLifeView.setMode(GameOfLifeView.State.MOVING);
    }
    
}
