package be.ppareit.gameoflife

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GameDrawer(
    onNew: () -> Unit,
    onRandomize: () -> Unit,
    onLoadSeed: () -> Unit,
    onLoadFromFile: () -> Unit,
    onSaveToFile: () -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit,
) {
    ModalDrawerSheet {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(24.dp),
        )
        HorizontalDivider()
        DrawerAction(stringResource(R.string.new_label), onNew)
        DrawerAction(stringResource(R.string.randomize_label), onRandomize)
        DrawerAction(stringResource(R.string.load_seed_label), onLoadSeed)
        DrawerAction(stringResource(R.string.load_from_file_label), onLoadFromFile)
        DrawerAction(stringResource(R.string.save_to_file_label), onSaveToFile)
        DrawerAction(stringResource(R.string.settings_label), onSettings)
        DrawerAction(stringResource(R.string.about_label), onAbout)
    }
}

@Composable
private fun DrawerAction(label: String, onClick: () -> Unit) {
    NavigationDrawerItem(
        label = { Text(label) },
        selected = false,
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
    )
}
