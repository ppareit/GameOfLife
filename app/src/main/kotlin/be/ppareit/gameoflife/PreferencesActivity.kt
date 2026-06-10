package be.ppareit.gameoflife

import android.os.Bundle
import android.preference.PreferenceActivity

@Suppress("DEPRECATION")
class PreferencesActivity : PreferenceActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.settings)

        findPreference("reset_population_settings").setOnPreferenceClickListener {
            Settings.getSettings(this).resetPopulationSettings()
            recreate()
            true
        }
    }
}
