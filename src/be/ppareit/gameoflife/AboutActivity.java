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
package be.ppareit.gameoflife;

import be.ppareit.gameoflife.R;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Simple activity that displays the about box.
 *
 */
public class AboutActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        // TODO: Html formatting is lost due to the call to String.format()
        String version = GolApplication.getVersion();
        TextView aboutText = (TextView) findViewById(R.id.about_content);
        String aboutString = getString(R.string.about_text);
        aboutString = String.format(aboutString, version);
        aboutText.setText(aboutString);
    }

}
