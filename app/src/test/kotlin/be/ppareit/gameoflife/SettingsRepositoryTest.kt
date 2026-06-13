package be.ppareit.gameoflife

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SettingsRepositoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun settingsEmitsDefaultsForEmptyStore() = runTest {
        val repository = SettingsRepository(testDataStore())

        assertEquals(GameSettings(), repository.settings.first())
    }

    @Test
    fun settingsEmitsTypedValuesWrittenThroughRepository() = runTest {
        val dataStore = testDataStore()
        val repository = SettingsRepository(dataStore)

        repository.setMinimumVariable(1)
        repository.setMaximumVariable(4)
        repository.setSpawnVariable(5)
        repository.setAnimationSpeed(15)
        repository.setDisplayTheme("dark")

        assertEquals(
            GameSettings(
                minimumVariable = 1,
                maximumVariable = 4,
                spawnVariable = 5,
                animationSpeed = 15,
                displayThemeId = "dark",
            ),
            repository.settings.first(),
        )
    }

    @Test
    fun numericWritesUseIntKeys() = runTest {
        val dataStore = testDataStore()
        val repository = SettingsRepository(dataStore)

        repository.setMinimumVariable(4)

        val preferences = dataStore.data.first()
        assertEquals(4, preferences[intPreferencesKey("minimum_variable_int")])
        assertNull(preferences[stringPreferencesKey("minimum_variable")])
    }

    @Test
    fun invalidNumericWritesFallBackToDefaults() = runTest {
        val repository = SettingsRepository(testDataStore())

        repository.setMinimumVariable(7)
        repository.setMaximumVariable(-1)
        repository.setSpawnVariable(99)
        repository.setAnimationSpeed(11)

        assertEquals(GameSettings(), repository.settings.first())
    }

    @Test
    fun invalidPersistedNumericValuesFallBackToDefaults() = runTest {
        val dataStore = testDataStore()
        dataStore.edit { preferences ->
            preferences[intPreferencesKey("minimum_variable_int")] = 7
            preferences[intPreferencesKey("maximum_variable_int")] = -1
            preferences[intPreferencesKey("spawn_variable_int")] = 99
            preferences[intPreferencesKey("animation_speed_int")] = 11
        }
        val repository = SettingsRepository(dataStore)

        assertEquals(GameSettings(), repository.settings.first())
    }

    @Test
    fun legacyStringValuesAreReadWhenTypedValuesAreAbsent() = runTest {
        val dataStore = testDataStore()
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("minimum_variable")] = "1"
            preferences[stringPreferencesKey("maximum_variable")] = "4"
            preferences[stringPreferencesKey("spawn_variable")] = "5"
            preferences[stringPreferencesKey("animation_speed")] = "20"
            preferences[stringPreferencesKey("display_theme")] = "dark"
        }
        val repository = SettingsRepository(dataStore)

        assertEquals(
            GameSettings(
                minimumVariable = 1,
                maximumVariable = 4,
                spawnVariable = 5,
                animationSpeed = 20,
                displayThemeId = "dark",
            ),
            repository.settings.first(),
        )
    }

    @Test
    fun typedValuesOverrideLegacyStringValues() = runTest {
        val dataStore = testDataStore()
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("minimum_variable")] = "1"
            preferences[intPreferencesKey("minimum_variable_int")] = 4
        }
        val repository = SettingsRepository(dataStore)

        assertEquals(4, repository.settings.first().minimumVariable)
    }

    @Test
    fun resetPopulationSettingsRestoresPopulationDefaultsOnly() = runTest {
        val repository = SettingsRepository(testDataStore())
        repository.setMinimumVariable(1)
        repository.setMaximumVariable(4)
        repository.setSpawnVariable(5)
        repository.setAnimationSpeed(20)
        repository.setDisplayTheme("dark")

        repository.resetPopulationSettings()

        assertEquals(
            GameSettings(
                animationSpeed = 20,
                displayThemeId = "dark",
            ),
            repository.settings.first(),
        )
    }

    @Test
    fun unknownThemeFallsBackToDefault() = runTest {
        val repository = SettingsRepository(testDataStore())

        repository.setDisplayTheme("missing")

        assertEquals(BoardThemes.default().id, repository.settings.first().displayThemeId)
    }

    private fun TestScope.testDataStore(): DataStore<Preferences> {
        val file = File(temporaryFolder.newFolder(), "settings.preferences_pb")
        return PreferenceDataStoreFactory.create(scope = backgroundScope) { file }
    }
}
