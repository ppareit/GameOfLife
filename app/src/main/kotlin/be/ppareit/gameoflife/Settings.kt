package be.ppareit.gameoflife

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private const val OLD_SHARED_PREFERENCES_SUFFIX = "_preferences"
private const val SETTINGS_DATASTORE_NAME = "settings"

private const val KEY_MINIMUM_VARIABLE = "minimum_variable"
private const val KEY_MAXIMUM_VARIABLE = "maximum_variable"
private const val KEY_SPAWN_VARIABLE = "spawn_variable"
private const val KEY_ANIMATION_SPEED = "animation_speed"
private const val KEY_DISPLAY_THEME = "display_theme"
private const val KEY_MINIMUM_VARIABLE_INT = "minimum_variable_int"
private const val KEY_MAXIMUM_VARIABLE_INT = "maximum_variable_int"
private const val KEY_SPAWN_VARIABLE_INT = "spawn_variable_int"
private const val KEY_ANIMATION_SPEED_INT = "animation_speed_int"

private val POPULATION_RANGE = 0..6
private val ANIMATION_SPEEDS = setOf(2, 6, 10, 15, 20)

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = SETTINGS_DATASTORE_NAME,
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, "${context.packageName}$OLD_SHARED_PREFERENCES_SUFFIX"))
    },
)

data class GameSettings(
    val minimumVariable: Int = 2,
    val maximumVariable: Int = 3,
    val spawnVariable: Int = 3,
    val animationSpeed: Int = 10,
    val displayThemeId: String = BoardThemes.default().id,
) {
    val rows: Int = 60
    val cols: Int = 60
    val displayTheme: BoardThemeSpec = BoardThemes.findById(displayThemeId)
}

private object GameSettingKeys {
    val minimumVariable = intPreferencesKey(KEY_MINIMUM_VARIABLE_INT)
    val maximumVariable = intPreferencesKey(KEY_MAXIMUM_VARIABLE_INT)
    val spawnVariable = intPreferencesKey(KEY_SPAWN_VARIABLE_INT)
    val animationSpeed = intPreferencesKey(KEY_ANIMATION_SPEED_INT)
    val displayTheme = stringPreferencesKey(KEY_DISPLAY_THEME)

    val legacyMinimumVariable = stringPreferencesKey(KEY_MINIMUM_VARIABLE)
    val legacyMaximumVariable = stringPreferencesKey(KEY_MAXIMUM_VARIABLE)
    val legacySpawnVariable = stringPreferencesKey(KEY_SPAWN_VARIABLE)
    val legacyAnimationSpeed = stringPreferencesKey(KEY_ANIMATION_SPEED)
}

class SettingsRepository internal constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private val defaults = GameSettings()

    constructor(context: Context) : this(context.applicationContext.settingsDataStore)

    val settings: Flow<GameSettings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            GameSettings(
                minimumVariable = preferences.intValue(
                    key = GameSettingKeys.minimumVariable,
                    legacyKey = GameSettingKeys.legacyMinimumVariable,
                    defaultValue = defaults.minimumVariable,
                    validValues = POPULATION_RANGE,
                ),
                maximumVariable = preferences.intValue(
                    key = GameSettingKeys.maximumVariable,
                    legacyKey = GameSettingKeys.legacyMaximumVariable,
                    defaultValue = defaults.maximumVariable,
                    validValues = POPULATION_RANGE,
                ),
                spawnVariable = preferences.intValue(
                    key = GameSettingKeys.spawnVariable,
                    legacyKey = GameSettingKeys.legacySpawnVariable,
                    defaultValue = defaults.spawnVariable,
                    validValues = POPULATION_RANGE,
                ),
                animationSpeed = preferences.intValue(
                    key = GameSettingKeys.animationSpeed,
                    legacyKey = GameSettingKeys.legacyAnimationSpeed,
                    defaultValue = defaults.animationSpeed,
                    validValues = ANIMATION_SPEEDS,
                ),
                displayThemeId = BoardThemes.findById(
                    preferences[GameSettingKeys.displayTheme] ?: defaults.displayThemeId,
                ).id,
            )
        }

    suspend fun setMinimumVariable(value: Int) = setInt(
        key = GameSettingKeys.minimumVariable,
        value = value,
        defaultValue = defaults.minimumVariable,
        validValues = POPULATION_RANGE,
    )

    suspend fun setMaximumVariable(value: Int) = setInt(
        key = GameSettingKeys.maximumVariable,
        value = value,
        defaultValue = defaults.maximumVariable,
        validValues = POPULATION_RANGE,
    )

    suspend fun setSpawnVariable(value: Int) = setInt(
        key = GameSettingKeys.spawnVariable,
        value = value,
        defaultValue = defaults.spawnVariable,
        validValues = POPULATION_RANGE,
    )

    suspend fun setAnimationSpeed(value: Int) = setInt(
        key = GameSettingKeys.animationSpeed,
        value = value,
        defaultValue = defaults.animationSpeed,
        validValues = ANIMATION_SPEEDS,
    )

    suspend fun setDisplayTheme(themeId: String) {
        dataStore.edit { preferences ->
            preferences[GameSettingKeys.displayTheme] = BoardThemes.findById(themeId).id
        }
    }

    suspend fun resetPopulationSettings() {
        dataStore.edit { preferences ->
            preferences[GameSettingKeys.minimumVariable] = defaults.minimumVariable
            preferences[GameSettingKeys.maximumVariable] = defaults.maximumVariable
            preferences[GameSettingKeys.spawnVariable] = defaults.spawnVariable
        }
    }

    private suspend fun setInt(
        key: Preferences.Key<Int>,
        value: Int,
        defaultValue: Int,
        validValues: Iterable<Int>,
    ) {
        dataStore.edit { preferences ->
            preferences[key] = value.takeIf { it in validValues } ?: defaultValue
        }
    }

    private fun Preferences.intValue(
        key: Preferences.Key<Int>,
        legacyKey: Preferences.Key<String>,
        defaultValue: Int,
        validValues: Iterable<Int>,
    ): Int {
        val value = this[key] ?: this[legacyKey]?.toIntOrNull()
        return value?.takeIf { it in validValues } ?: defaultValue
    }
}
