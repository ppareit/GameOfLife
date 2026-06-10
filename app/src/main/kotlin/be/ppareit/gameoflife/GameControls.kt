package be.ppareit.gameoflife

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun GameControls(
    mode: GameOfLifeView.State,
    canUndo: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onStep: () -> Unit,
    onUndo: () -> Unit,
    onMove: () -> Unit,
    onEdit: () -> Unit,
) {
    Row(
        modifier = Modifier.padding(end = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (mode == GameOfLifeView.State.RUNNING) {
            ToolbarText(stringResource(R.string.pause_label), onPause)
        } else {
            ToolbarText(stringResource(R.string.start_label), onStart)
            ToolbarText(stringResource(R.string.step_label), onStep)
            ToolbarText(stringResource(R.string.undo_label), onUndo, enabled = canUndo)
            Spacer(Modifier.width(4.dp))
            ToolbarText(label = if (mode == GameOfLifeView.State.EDITING) {
                stringResource(R.string.edit_label)
            } else {
                stringResource(R.string.move_label)
            }, onClick = {
                if (mode == GameOfLifeView.State.EDITING) {
                    onMove()
                } else {
                    onEdit()
                }
            })
        }
    }
}

@Composable
private fun ToolbarText(label: String, onClick: () -> Unit, enabled: Boolean = true) {
    Text(
        text = label,
        color = if (enabled) Color.White else Color(0xFF9E9E9E),
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 12.dp),
    )
}
