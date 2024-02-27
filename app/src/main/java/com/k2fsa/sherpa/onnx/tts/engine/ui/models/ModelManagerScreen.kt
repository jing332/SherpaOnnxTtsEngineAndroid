package com.k2fsa.sherpa.onnx.tts.engine.ui.models

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.k2fsa.sherpa.onnx.OfflineTtsConfig
import com.k2fsa.sherpa.onnx.tts.engine.R
import com.k2fsa.sherpa.onnx.tts.engine.synthesizer.ModelManager
import com.k2fsa.sherpa.onnx.tts.engine.synthesizer.ModelManager.toOfflineTtsConfig
import com.k2fsa.sherpa.onnx.tts.engine.synthesizer.config.Model
import com.k2fsa.sherpa.onnx.tts.engine.ui.AuditionDialog
import kotlinx.coroutines.flow.map

@Composable
fun ModelManagerScreen(modifier: Modifier = Modifier) {
    val vm: ModelManagerViewModel = viewModel()
    LaunchedEffect(key1 = vm) {
        vm.load()
    }

    val context = LocalContext.current
    LaunchedEffect(key1 = vm.models.value) {
        println("models: ${vm.models.value}")
    }

    var showAuditionDialog by remember { mutableStateOf<OfflineTtsConfig?>(null) }
    if (showAuditionDialog != null) {
        AuditionDialog(
            onDismissRequest = { showAuditionDialog = null },
            offlineTtsConfig = showAuditionDialog!!
        )
    }

    var showModelEditDialog by remember { mutableStateOf<Model?>(null) }
    if (showModelEditDialog != null) {
        ModelEditDialog(
            onDismissRequest = { showModelEditDialog = null },
            model = showModelEditDialog!!,
            onSave = { ModelManager.updateModel(it) }
        )
    }

    LazyColumn(modifier) {
        items(vm.models.value) { model ->
            ModelItem(name = model.name, onAudition = {
                showAuditionDialog = model.toOfflineTtsConfig()
            }, onEdit = {
                showModelEditDialog = model
            })
        }
    }
}

@Composable
private fun ModelItem(name: String, onAudition: () -> Unit, onEdit: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            modifier = Modifier.weight(1f),
            text = name,
            style = MaterialTheme.typography.titleMedium
        )
        Row {
            IconButton(onClick = onAudition) {
                Icon(
                    Icons.Default.Headset,
                    contentDescription = stringResource(id = R.string.audition)
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = stringResource(id = R.string.edit))
            }
        }
    }
}