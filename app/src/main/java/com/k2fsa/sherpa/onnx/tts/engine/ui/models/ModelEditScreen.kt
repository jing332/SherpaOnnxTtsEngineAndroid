package com.k2fsa.sherpa.onnx.tts.engine.ui.models

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.k2fsa.sherpa.onnx.tts.engine.ui.widgets.DenseOutlinedField

@Composable
fun ModelEditScreen(
    modifier: Modifier = Modifier,
    model: Model,
    onModelChange: (Model) -> Unit
) {
    Column(modifier) {
        DenseOutlinedField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            value = model.id,
            onValueChange = {
                onModelChange(model.copy(id = it))
            },
            label = { Text(text = "ID") }
        )

        DenseOutlinedField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            value = model.name,
            onValueChange = {
                onModelChange(model.copy(name = it))
            },
            label = { Text(text = stringResource(R.string.display_name)) }
        )

        DenseOutlinedField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            value = model.onnx,
            onValueChange = {
                onModelChange(model.copy(onnx = it))
            },
            label = { Text(text = stringResource(R.string.onnx_model_file)) }
        )

        var showLangSelectDialog by remember { mutableStateOf(false) }
        if (showLangSelectDialog)
            ModelLanguageSelectionDialog(
                onDismissRequest = { showLangSelectDialog = false },
                language = model.lang
            ) {
                onModelChange(model.copy(lang = it))
                showLangSelectDialog = false
            }

        DenseOutlinedField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            value = model.lang,
            onValueChange = {
                onModelChange(model.copy(lang = it))
            },
            label = { Text(text = stringResource(R.string.language)) },
            trailingIcon = {
                IconButton(onClick = { showLangSelectDialog = true }) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = stringResource(id = R.string.language)
                    )
                }
            }
        )
    }
}