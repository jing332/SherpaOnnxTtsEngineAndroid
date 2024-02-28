package com.k2fsa.sherpa.onnx.tts.engine.ui.models

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddToPhotos
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.k2fsa.sherpa.onnx.OfflineTtsConfig
import com.k2fsa.sherpa.onnx.tts.engine.R
import com.k2fsa.sherpa.onnx.tts.engine.conf.TtsConfig
import com.k2fsa.sherpa.onnx.tts.engine.synthesizer.ModelManager
import com.k2fsa.sherpa.onnx.tts.engine.synthesizer.ModelManager.toOfflineTtsConfig
import com.k2fsa.sherpa.onnx.tts.engine.synthesizer.config.Model
import com.k2fsa.sherpa.onnx.tts.engine.ui.AuditionDialog
import com.k2fsa.sherpa.onnx.tts.engine.ui.SampleTextEditDialog
import com.k2fsa.sherpa.onnx.tts.engine.utils.toLocale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelManagerScreen() {
    var showSampleTextEditDialog by remember { mutableStateOf(false) }
    if (showSampleTextEditDialog)
        SampleTextEditDialog { showSampleTextEditDialog = false }

    var showImportDialog by remember { mutableStateOf(false) }
    if (showImportDialog)
        ImportModelsDialog { showImportDialog = false }

    Scaffold(topBar = {
        TopAppBar(title = { Text("Next-gen Kaldi: TTS") }, actions = {
            IconButton(onClick = { showSampleTextEditDialog = true }) {
                Icon(Icons.Default.TextFields, stringResource(R.string.sample_text))
            }

            IconButton(onClick = { showImportDialog = true }) {
                Icon(
                    Icons.Default.AddToPhotos,
                    stringResource(R.string.import_models)
                )
            }
        })
    }) {
        ModelManagerScreenContent(Modifier.padding(it))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ModelManagerScreenContent(modifier: Modifier = Modifier) {
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
        items(vm.models.value, { it.id }) { model ->
            val lang = remember(model.lang) { model.lang.toLocale().displayName }
            val selected = TtsConfig.modelId.value == model.id
            ModelItem(
                modifier = Modifier
                    .animateItemPlacement()
                    .padding(4.dp),
                name = model.name, lang = lang + " (${model.lang})",
                selected = selected,
                onAudition = {
                    showAuditionDialog = model.toOfflineTtsConfig()
                },
                onEdit = {
                    showModelEditDialog = model
                },
                onClick = {
                    TtsConfig.modelId.value = model.id
                }
            )
        }
    }
}

@Composable
private fun ModelItem(
    modifier: Modifier,
    name: String,
    lang: String,
    selected: Boolean,
    onClick: () -> Unit,
    onAudition: () -> Unit,
    onEdit: () -> Unit
) {
    ElevatedCard(
        modifier = modifier.semantics {
            this.selected = selected
        },
        colors = if (selected) CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        else CardDefaults.elevatedCardColors(),
        onClick = onClick
    ) {
        Box(modifier = Modifier.padding(4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = lang,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Row {
                    IconButton(onClick = onAudition) {
                        Icon(
                            Icons.Default.Headset,
                            contentDescription = stringResource(id = R.string.audition)
                        )
                    }
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = stringResource(id = R.string.edit)
                        )
                    }
                }
            }
        }
    }
}
