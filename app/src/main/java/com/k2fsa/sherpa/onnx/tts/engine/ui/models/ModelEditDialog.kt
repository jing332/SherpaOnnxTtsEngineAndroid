package com.k2fsa.sherpa.onnx.tts.engine.ui.models

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.k2fsa.sherpa.onnx.tts.engine.R
import com.k2fsa.sherpa.onnx.tts.engine.synthesizer.config.Model
import com.k2fsa.sherpa.onnx.tts.engine.ui.widgets.AppDialog

@Composable
fun ModelEditDialog(
    onDismissRequest: () -> Unit,
    model: Model,
    onSave: (Model) -> Unit
) {
    var data by remember { mutableStateOf(model) }
    AppDialog(onDismissRequest = onDismissRequest, title = {
        Text(stringResource(R.string.edit))
    }, content = {
        ModelEditScreen(
            model = data,
            onModelChange = {
                data = it
            }
        )
    }, buttons = {
        Row(horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onDismissRequest) { Text(stringResource(id = android.R.string.cancel)) }
            TextButton(onClick = {
                onSave(data.copy())
                onDismissRequest()
            }) { Text(stringResource(id = android.R.string.ok)) }
        }
    })
}