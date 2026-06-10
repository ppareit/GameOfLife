package be.ppareit.gameoflife

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun SeedPickerDialog(
    seeds: List<String>,
    onDismiss: () -> Unit,
    onSeedSelected: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        title = { Text(stringResource(R.string.select_seed)) },
        text = {
            if (seeds.isEmpty()) {
                Text(stringResource(R.string.no_seeds_found))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                ) {
                    items(seeds) { seed ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSeedSelected(seed) }
                                .padding(vertical = 14.dp),
                        ) {
                            Text(seed)
                        }
                    }
                }
            }
        },
    )
}
