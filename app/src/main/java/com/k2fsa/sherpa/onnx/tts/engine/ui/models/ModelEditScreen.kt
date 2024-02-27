package com.k2fsa.sherpa.onnx.tts.engine.ui.models

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.k2fsa.sherpa.onnx.tts.engine.synthesizer.config.Model
import com.k2fsa.sherpa.onnx.tts.engine.ui.widgets.DenseOutlinedField

@Composable
fun ModelEditScreen(
    modifier: Modifier = Modifier,
    model: Model,
    onModelChange: (Model) -> Unit
) {
    Column(modifier) {
        DenseOutlinedField(
            value = model.id,
            onValueChange = {
                onModelChange(model.copy(id = it))
            },
            label = { Text(text = "ID") }
        )

        DenseOutlinedField(
            value = model.name,
            onValueChange = {
                onModelChange(model.copy(name = it))
            },
            label = { Text(text = "Display name") }
        )

        DenseOutlinedField(
            value = model.onnx,
            onValueChange = {
                onModelChange(model.copy(onnx = it))
            },
            label = { Text(text = "Onnx") }
        )
    }
}