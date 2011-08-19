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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;

public class PreferencesActivity extends PreferenceActivity {
    
    private static final String OPTION_MINIMUM = "UNDERPOPULATION_VARIABLE";
    private static final String OPTION_MINIMUM_DEFAULT = "2";
    private static final String OPTION_MAXIMUM = "OVERPOPULATION_VARIABLE";
    private static final String OPTION_MAXIMUM_DEFAULT = "3";
    private static final String OPTION_SPAWN = "SPAWN_VARIABLE";
    private static final String OPTION_SPAWN_DEFAULT = "3";
    private static final String OPTION_ANIMATION_SPEED = "ANIMATION_SPEED";
    private static final String OPTION_ANIMATION_SPEED_DEFAULT = "10";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        
        Preference resetPopulationSettings = findPreference("RESET_POPULATION_SETTINGS");
        resetPopulationSettings.setOnPreferenceClickListener(
                new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(
                        PreferencesActivity.this).edit();
                prefs.putString(OPTION_MINIMUM, OPTION_MINIMUM_DEFAULT);
                prefs.putString(OPTION_MAXIMUM, OPTION_MAXIMUM_DEFAULT);
                prefs.putString(OPTION_SPAWN, OPTION_SPAWN_DEFAULT);
                prefs.commit();
                recreate();
                return true;
            }
        });
    }
    
    public static int getMinimumVariable(Context context) {
        return Integer.parseInt(
                PreferenceManager.getDefaultSharedPreferences(context).
                getString(OPTION_MINIMUM, OPTION_MINIMUM_DEFAULT));
    }

    public static int getMaximumVariable(Context context) {
        return Integer.parseInt(
                PreferenceManager.getDefaultSharedPreferences(context).
                getString(OPTION_MAXIMUM, OPTION_MAXIMUM_DEFAULT));
    }

    public static int getSpawnVariable(Context context) {
        return Integer.parseInt(
                PreferenceManager.getDefaultSharedPreferences(context).
                getString(OPTION_SPAWN, OPTION_SPAWN_DEFAULT));
    }
    
    public static int getAnimationSpeed(Context context) {
        return Integer.parseInt(
                PreferenceManager.getDefaultSharedPreferences(context).
                getString(OPTION_ANIMATION_SPEED, OPTION_ANIMATION_SPEED_DEFAULT));
    }
    
    public static int getDisplayTheme(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int ndx = Integer.parseInt(prefs.getString("DISPLAY_THEME", "0"));
        Resources res = context.getResources();
        TypedArray themes = res.obtainTypedArray(R.array.themes);
        return themes.getResourceId(ndx, R.array.dark_theme);
    }

}
