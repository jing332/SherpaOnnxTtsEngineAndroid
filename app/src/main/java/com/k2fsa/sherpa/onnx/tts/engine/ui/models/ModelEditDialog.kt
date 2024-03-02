package com.k2fsa.sherpa.onnx.tts.engine.ui.models

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.k2fsa.sherpa.onnx.tts.engine.R
import com.k2fsa.sherpa.onnx.tts.engine.synthesizer.config.Model
import com.k2fsa.sherpa.onnx.tts.engine.ui.widgets.AppDialog
import com.k2fsa.sherpa.onnx.tts.engine.ui.widgets.CancelButton
import com.k2fsa.sherpa.onnx.tts.engine.ui.widgets.OkButton

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
        Row {
            CancelButton(onClick = onDismissRequest)
            OkButton(Modifier.padding(start = 4.dp), onClick = {
                onSave(data.copy())
                onDismissRequest()
            })
        }
    })
}