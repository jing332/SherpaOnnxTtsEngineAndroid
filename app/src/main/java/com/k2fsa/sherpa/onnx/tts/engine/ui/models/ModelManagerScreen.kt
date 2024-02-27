package com.k2fsa.sherpa.onnx.tts.engine.ui.models

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.k2fsa.sherpa.onnx.OfflineTtsConfig
import com.k2fsa.sherpa.onnx.tts.engine.synthesizer.ModelManager
import com.k2fsa.sherpa.onnx.tts.engine.synthesizer.ModelManager.toOfflineTtsConfig
import com.k2fsa.sherpa.onnx.tts.engine.ui.AuditionDialog

@Composable
fun ModelManagerScreen(modifier: Modifier = Modifier) {
    val vm: ModelManagerViewModel = viewModel()
    LaunchedEffect(key1 = vm) {
        vm.load()
    }

    val context = LocalContext.current
    val models by ModelManager.modelsFlow.collectAsState(initial = emptyList())

    var showAuditionDialog by remember { mutableStateOf<OfflineTtsConfig?>(null) }
    if (showAuditionDialog != null) {
        AuditionDialog(
            onDismissRequest = { showAuditionDialog = null },
            offlineTtsConfig = showAuditionDialog!!
        )
    }

    LazyColumn(modifier) {
        items(models) { model ->
            ModelItem(name = model.name, onAudition = {
                showAuditionDialog = model.toOfflineTtsConfig()
            })
        }
    }
}

@Composable
private fun ModelItem(name: String, onAudition: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            modifier = Modifier.weight(1f),
            text = name,
            style = MaterialTheme.typography.titleMedium
        )
        Row {
            IconButton(onClick = onAudition) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play")
            }
            IconButton(onClick = { /*TODO*/ }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
        }
    }
}