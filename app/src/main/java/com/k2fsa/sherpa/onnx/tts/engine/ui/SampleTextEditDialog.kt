package com.k2fsa.sherpa.onnx.tts.engine.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.k2fsa.sherpa.onnx.tts.engine.conf.AppConfig
import com.k2fsa.sherpa.onnx.tts.engine.R
import com.k2fsa.sherpa.onnx.tts.engine.ui.widgets.DenseOutlinedField

@Composable
fun SampleTextEditDialog(onDismissRequest: () -> Unit) {
    var text by remember { mutableStateOf(AppConfig.sampleText.value) }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = stringResource(id = R.string.sample_text))
        },
        text = {
            DenseOutlinedField(
                modifier = Modifier.fillMaxWidth(),
                value = text,
                onValueChange = { text = it })
        },
        confirmButton = {
            TextButton(onClick = {
                AppConfig.sampleText.value = text
            }) {
                Text(text = stringResource(id = android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
        }
    )
}