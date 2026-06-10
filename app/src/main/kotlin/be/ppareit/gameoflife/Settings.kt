package be.ppareit.gameoflife

import android.content.Context
import android.preference.PreferenceManager

class Settings private constructor(context: Context) {
    private val pref = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
    val cols: Int = 60
    val rows: Int = 60

    fun resetPopulationSettings() {
        pref.edit()
            .putString("minimum_variable", "2")
            .putString("maximum_variable", "3")
            .putString("spawn_variable", "3")
            .apply()
    }

    fun getMinimumVariable(): Int = getStringAsInt("minimum_variable", 2)
    fun getMaximumVariable(): Int = getStringAsInt("maximum_variable", 3)
    fun getSpawnVariable(): Int = getStringAsInt("spawn_variable", 3)
    fun getAnimationSpeed(): Int = getStringAsInt("animation_speed", 10)

    fun getDisplayTheme(): Int {
        val themeIndex = pref.getString("display_theme", "0")?.toIntOrNull() ?: 0
        val themes = App.app.resources.obtainTypedArray(R.array.themes)
        return try {
            themes.getResourceId(themeIndex, R.array.dark_theme)
        } finally {
            themes.recycle()
        }
    }

    private fun getStringAsInt(key: String, defaultValue: Int): Int {
        return pref.getString(key, defaultValue.toString())?.toIntOrNull() ?: defaultValue
    }

    companion object {
        fun getSettings(context: Context): Settings = Settings(context)
    }
}
