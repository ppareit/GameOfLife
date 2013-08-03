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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.preference.PreferenceManager;

public class Settings {
    private static final String OPTION_MINIMUM = "UNDERPOPULATION_VARIABLE";
    private static final String OPTION_MINIMUM_DEFAULT = "2";
    private static final String OPTION_MAXIMUM = "OVERPOPULATION_VARIABLE";
    private static final String OPTION_MAXIMUM_DEFAULT = "3";
    private static final String OPTION_SPAWN = "SPAWN_VARIABLE";
    private static final String OPTION_SPAWN_DEFAULT = "3";
    private static final String OPTION_ANIMATION_SPEED = "ANIMATION_SPEED";
    private static final String OPTION_ANIMATION_SPEED_DEFAULT = "10";
    private static final String OPTION_DISPLAY_THEME = "DISPLAY_THEME";
    private static final String OPTION_DISPLAY_THEME_DEFAULT = "0";

    public static void resetPopulationSettings() {
        Context context = GolApplication.getAppContext();
        SharedPreferences sprefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor pedit = sprefs.edit();
        pedit.putString(OPTION_MINIMUM, OPTION_MINIMUM_DEFAULT);
        pedit.putString(OPTION_MAXIMUM, OPTION_MAXIMUM_DEFAULT);
        pedit.putString(OPTION_SPAWN, OPTION_SPAWN_DEFAULT);
        pedit.commit();
    }

    public static int getMinimumVariable() {
        Context context = GolApplication.getAppContext();
        SharedPreferences sprefs = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.parseInt(sprefs.getString(OPTION_MINIMUM, OPTION_MINIMUM_DEFAULT));
    }

    public static int getMaximumVariable() {
        Context context = GolApplication.getAppContext();
        SharedPreferences sprefs = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.parseInt(sprefs.getString(OPTION_MAXIMUM, OPTION_MAXIMUM_DEFAULT));
    }

    public static int getSpawnVariable() {
        Context context = GolApplication.getAppContext();
        SharedPreferences sprefs = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.parseInt(sprefs.getString(OPTION_SPAWN, OPTION_SPAWN_DEFAULT));
    }

    public static int getAnimationSpeed() {
        Context context = GolApplication.getAppContext();
        SharedPreferences sprefs = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.parseInt(sprefs.getString(OPTION_ANIMATION_SPEED,
                OPTION_ANIMATION_SPEED_DEFAULT));
    }

    public static int getDisplayTheme() {
        Context context = GolApplication.getAppContext();
        SharedPreferences sprefs = PreferenceManager.getDefaultSharedPreferences(context);
        // first get the index of the theme
        String themeNdx = sprefs.getString(OPTION_DISPLAY_THEME,
                OPTION_DISPLAY_THEME_DEFAULT);
        int ndx = Integer.parseInt(themeNdx);
        // now convert that index to a real resource
        Resources res = context.getResources();
        TypedArray themes = res.obtainTypedArray(R.array.themes);
        return themes.getResourceId(ndx, R.array.dark_theme);
    }

}
