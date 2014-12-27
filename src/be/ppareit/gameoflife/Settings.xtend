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

import org.xtendroid.annotations.AndroidPreference

import static be.ppareit.gameoflife.App.*

@AndroidPreference class Settings {

    def resetPopulationSettings() {
        var pedit = pref.edit();
        pedit.putString("minimum_variable", "2");
        pedit.putString("maximum_variable", "3");
        pedit.putString("spawn_variable", "3");
        pedit.commit();
    }

    def getMinimumVariable() {
        return getStringAsInt("minimum_variable", 2)
    }
    def getMaximumVariable() {
        return getStringAsInt("maximum_variable", 3)
    }
    def getSpawnVariable() {
        return getStringAsInt("spawn_variable", 3)
    }

    def getAnimationSpeed() {
        return getStringAsInt("animation_speed", 10)
    }

    def getDisplayTheme() {
        // first get the index of the theme
        var themeNdx = pref.getString("display_theme", "0");
        var ndx = Integer.parseInt(themeNdx);
        // now convert that index to a real resource
        var themes = app.resources.obtainTypedArray(R.array.themes);
        return themes.getResourceId(ndx, R.array.dark_theme);
    }

    private def getStringAsInt(String key, int defVal) {
        val asString = pref.getString(key, defVal.toString())
        val asInt = Integer.valueOf(asString)
        return asInt
    }

}
