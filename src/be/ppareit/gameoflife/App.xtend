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

import android.app.Application
import android.content.pm.PackageManager.NameNotFoundException
import android.util.Log
import org.xtendroid.annotations.AddLogTag

@AddLogTag
public class App extends Application {

    private static App sApp
    
    static def App getApp() {
        return sApp
    }

    override void onCreate() {
        super.onCreate()
        sApp = this
    }

    /**
     * Get the version from the manifest.
     *
     * @return The version as a String.
     */
    static def getVersion() {
        val context = app.applicationContext
        val packageName = context.getPackageName()
        try {
            val pm = context.getPackageManager()
            return pm.getPackageInfo(packageName, 0).versionName
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Unable to find the name " + packageName + " in the package")
            return ""
        }
    }

}
