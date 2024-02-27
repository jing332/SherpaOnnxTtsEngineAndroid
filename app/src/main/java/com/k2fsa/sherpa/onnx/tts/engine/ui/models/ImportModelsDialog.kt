package com.k2fsa.sherpa.onnx.tts.engine.ui.models

import android.text.Selection
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.k2fsa.sherpa.onnx.tts.engine.R
import com.k2fsa.sherpa.onnx.tts.engine.synthesizer.ModelConstants
import com.k2fsa.sherpa.onnx.tts.engine.synthesizer.ModelManager
import com.k2fsa.sherpa.onnx.tts.engine.synthesizer.config.Model

@Composable
fun ImportModelsDialog(onDismissRequest: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val models = remember { ModelManager.getNotAddedModels(context) }
    val checkedModels = remember { mutableStateListOf<Model>() }
    AlertDialog(onDismissRequest = onDismissRequest, title = {
        Column {
            Text(text = stringResource(id = R.string.import_models))
            SelectionContainer {
                Text(text = ModelConstants.modelPath, style = MaterialTheme.typography.bodySmall)
            }
        }
    }, text = {
        if (models.isEmpty())
            Text(
                text = stringResource(id = R.string.list_is_empty),
                color = MaterialTheme.colorScheme.error
            )

        LazyColumn {
            items(models) { model ->
                val checked = checkedModels.contains(model)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.Checkbox) {
                            if (checked) checkedModels.remove(model) else
                                checkedModels.add(model)
                        }
                        .minimumInteractiveComponentSize()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = checked, onCheckedChange = null)
                    Text(text = model.name, modifier = Modifier.padding(start = 4.dp))
                }
            }
        }
    }, confirmButton = {
        TextButton(onClick = {
            ModelManager.addModel(*checkedModels.toTypedArray())
        }) {
            Text(text = stringResource(id = android.R.string.ok))
        }
    }, dismissButton = {
        TextButton(onClick = onDismissRequest) {
            Text(text = stringResource(id = android.R.string.cancel))
        }
    })
}