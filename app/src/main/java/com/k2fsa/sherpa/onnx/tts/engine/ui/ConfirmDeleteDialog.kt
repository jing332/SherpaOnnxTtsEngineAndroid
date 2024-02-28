package com.k2fsa.sherpa.onnx.tts.engine.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.k2fsa.sherpa.onnx.tts.engine.R

@Composable
fun ConfirmDeleteDialog(onDismissRequest: () -> Unit, name: String, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(id = R.string.is_confirm_delete)) },
        text = {
            Text(
                name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm()
            }) {
                Text(
                    stringResource(id = R.string.delete),
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(id = android.R.string.cancel))
            }
        },
        icon = {
            Icon(
                Icons.Default.DeleteForever,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        }
    )
}