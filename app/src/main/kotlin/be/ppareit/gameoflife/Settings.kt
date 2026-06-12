package be.ppareit.gameoflife

import android.content.Context
import android.content.SharedPreferences

class Settings private constructor(context: Context) {
    private val pref = getPreferences(context)
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

    fun getDisplayTheme(): BoardThemeSpec {
        return BoardThemes.findById(pref.getString("display_theme", BoardThemes.default().id))
    }

    private fun getStringAsInt(key: String, defaultValue: Int): Int {
        return pref.getString(key, defaultValue.toString())?.toIntOrNull() ?: defaultValue
    }

    companion object {
        fun getSettings(context: Context): Settings = Settings(context)

        fun getPreferences(context: Context): SharedPreferences {
            val appContext = context.applicationContext
            return appContext.getSharedPreferences("${appContext.packageName}_preferences", Context.MODE_PRIVATE)
        }
    }
}
