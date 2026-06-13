package be.ppareit.gameoflife

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {
    private val settingsRepository = App.settingsRepository

    val settings: Flow<GameSettings> = settingsRepository.settings

    fun setMinimumVariable(value: Int) {
        viewModelScope.launch {
            settingsRepository.setMinimumVariable(value)
        }
    }

    fun setMaximumVariable(value: Int) {
        viewModelScope.launch {
            settingsRepository.setMaximumVariable(value)
        }
    }

    fun setSpawnVariable(value: Int) {
        viewModelScope.launch {
            settingsRepository.setSpawnVariable(value)
        }
    }

    fun setAnimationSpeed(value: Int) {
        viewModelScope.launch {
            settingsRepository.setAnimationSpeed(value)
        }
    }

    fun setDisplayTheme(themeId: String) {
        viewModelScope.launch {
            settingsRepository.setDisplayTheme(themeId)
        }
    }

    fun resetPopulationSettings() {
        viewModelScope.launch {
            settingsRepository.resetPopulationSettings()
        }
    }
}
