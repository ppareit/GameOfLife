package be.ppareit.gameoflife

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SettingsDialog(onDismiss: () -> Unit) {
    val settingsViewModel: SettingsViewModel = viewModel()
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle(initialValue = GameSettings())
    val populationOptions = stringArrayResource(R.array.population_options).toList()
    val populationValues = stringArrayResource(R.array.population_values).toList()
    val speedOptions = stringArrayResource(R.array.animation_speed_options).toList()
    val speedValues = stringArrayResource(R.array.animation_speed_values).toList()
    val boardThemes = BoardThemes.all()
    val themeOptions = boardThemes.map { stringResource(it.labelRes) }
    val themeValues = boardThemes.map { it.id }
    val underpopulationTitle = stringResource(R.string.underpopulation_title)
    val overpopulationTitle = stringResource(R.string.overpopulation_title)
    val spawnTitle = stringResource(R.string.spawn_title)
    val speedTitle = stringResource(R.string.speed_title)
    val themeTitle = stringResource(R.string.theme_title)
    var activeChoice by remember { mutableStateOf<PreferenceChoice?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        },
        title = { Text(stringResource(R.string.settings_title)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                SettingsSection(stringResource(R.string.population_settings_title))
                PreferenceRow(
                    title = underpopulationTitle,
                    summary = stringResource(R.string.underpopulation_summary),
                    value = labelFor(settings.minimumVariable.toString(), populationValues, populationOptions),
                    onClick = {
                        activeChoice = PreferenceChoice(
                            title = underpopulationTitle,
                            values = populationValues,
                            labels = populationOptions,
                            selectedValue = settings.minimumVariable.toString(),
                            onSelected = { value ->
                                value.toIntOrNull()?.let { settingsViewModel.setMinimumVariable(it) }
                            },
                        )
                    },
                )
                PreferenceRow(
                    title = overpopulationTitle,
                    summary = stringResource(R.string.overpopulation_summary),
                    value = labelFor(settings.maximumVariable.toString(), populationValues, populationOptions),
                    onClick = {
                        activeChoice = PreferenceChoice(
                            title = overpopulationTitle,
                            values = populationValues,
                            labels = populationOptions,
                            selectedValue = settings.maximumVariable.toString(),
                            onSelected = { value ->
                                value.toIntOrNull()?.let { settingsViewModel.setMaximumVariable(it) }
                            },
                        )
                    },
                )
                PreferenceRow(
                    title = spawnTitle,
                    summary = stringResource(R.string.spawn_summary),
                    value = labelFor(settings.spawnVariable.toString(), populationValues, populationOptions),
                    onClick = {
                        activeChoice = PreferenceChoice(
                            title = spawnTitle,
                            values = populationValues,
                            labels = populationOptions,
                            selectedValue = settings.spawnVariable.toString(),
                            onSelected = { value ->
                                value.toIntOrNull()?.let { settingsViewModel.setSpawnVariable(it) }
                            },
                        )
                    },
                )
                PreferenceRow(
                    title = stringResource(R.string.reset_population_settings_title),
                    summary = stringResource(R.string.reset_population_settings_summary),
                    onClick = settingsViewModel::resetPopulationSettings,
                )
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                SettingsSection(stringResource(R.string.display_settings_title))
                PreferenceRow(
                    title = speedTitle,
                    value = labelFor(settings.animationSpeed.toString(), speedValues, speedOptions),
                    onClick = {
                        activeChoice = PreferenceChoice(
                            title = speedTitle,
                            values = speedValues,
                            labels = speedOptions,
                            selectedValue = settings.animationSpeed.toString(),
                            onSelected = { value ->
                                value.toIntOrNull()?.let { settingsViewModel.setAnimationSpeed(it) }
                            },
                        )
                    },
                )
                PreferenceRow(
                    title = themeTitle,
                    value = labelFor(settings.displayThemeId, themeValues, themeOptions),
                    onClick = {
                        activeChoice = PreferenceChoice(
                            title = themeTitle,
                            values = themeValues,
                            labels = themeOptions,
                            selectedValue = settings.displayThemeId,
                            onSelected = { value ->
                                settingsViewModel.setDisplayTheme(value)
                            },
                        )
                    },
                )
            }
        },
    )

    activeChoice?.let { choice ->
        PreferenceChoiceDialog(
            choice = choice,
            onDismiss = { activeChoice = null },
            onSelected = { value ->
                choice.onSelected(value)
                activeChoice = null
            },
        )
    }
}

@Composable
private fun SettingsSection(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
    )
}

@Composable
private fun PreferenceRow(
    title: String,
    summary: String? = null,
    value: String? = null,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            if (value != null) {
                Spacer(Modifier.width(16.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        if (summary != null) {
            Text(
                text = summary.trim(),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF666666),
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun PreferenceChoiceDialog(
    choice: PreferenceChoice,
    onDismiss: () -> Unit,
    onSelected: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        title = { Text(choice.title) },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp),
            ) {
                items(choice.values.zip(choice.labels)) { (value, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelected(value) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = value == choice.selectedValue,
                            onClick = { onSelected(value) },
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(label)
                    }
                }
            }
        },
    )
}

private data class PreferenceChoice(
    val title: String,
    val values: List<String>,
    val labels: List<String>,
    val selectedValue: String,
    val onSelected: (String) -> Unit,
)

private fun labelFor(value: String, values: List<String>, labels: List<String>): String {
    val index = values.indexOf(value)
    return labels.getOrNull(index) ?: value
}
