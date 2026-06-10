package be.ppareit.gameoflife

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import java.io.File

@Composable
fun SaveToFileDialog(
    onDismiss: () -> Unit,
    onSave: (Uri) -> Unit,
) {
    val context = LocalContext.current
    var fileName by remember { mutableStateOf("") }
    val trimmedFileName = fileName.trim()
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = trimmedFileName.isNotEmpty(),
                onClick = {
                    val saveFileName = if (trimmedFileName.endsWith(".life")) {
                        trimmedFileName
                    } else {
                        "$trimmedFileName.life"
                    }
                    onSave(Uri.fromFile(File(context.applicationInfo.dataDir, saveFileName)))
                },
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        title = { Text(stringResource(R.string.save_title)) },
        text = {
            OutlinedTextField(
                value = fileName,
                onValueChange = { fileName = it },
                singleLine = true,
                label = { Text(stringResource(R.string.file_name)) },
                modifier = Modifier.fillMaxWidth(),
            )
        },
    )
}
