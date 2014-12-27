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
package be.ppareit.gameoflife

import android.os.Bundle
import android.preference.PreferenceActivity

import static extension be.ppareit.gameoflife.Settings.* 

public class PreferencesActivity extends PreferenceActivity {

    override onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.settings)

        val resetPopulationSettings = findPreference("reset_population_settings")
        resetPopulationSettings.setOnPreferenceClickListener(
            [
                settings.resetPopulationSettings()
                recreate()
                return true
            ])
    }

}
