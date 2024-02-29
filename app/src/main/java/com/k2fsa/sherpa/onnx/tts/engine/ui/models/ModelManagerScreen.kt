package com.k2fsa.sherpa.onnx.tts.engine.ui.models

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.k2fsa.sherpa.onnx.OfflineTtsConfig
import com.k2fsa.sherpa.onnx.tts.engine.R
import com.k2fsa.sherpa.onnx.tts.engine.conf.TtsConfig
import com.k2fsa.sherpa.onnx.tts.engine.synthesizer.ModelManager
import com.k2fsa.sherpa.onnx.tts.engine.synthesizer.ModelManager.toOfflineTtsConfig
import com.k2fsa.sherpa.onnx.tts.engine.synthesizer.config.Model
import com.k2fsa.sherpa.onnx.tts.engine.ui.AuditionDialog
import com.k2fsa.sherpa.onnx.tts.engine.ui.ConfirmDeleteDialog
import com.k2fsa.sherpa.onnx.tts.engine.ui.ShadowReorderableItem
import com.k2fsa.sherpa.onnx.tts.engine.utils.performLongPress
import com.k2fsa.sherpa.onnx.tts.engine.utils.toLocale
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelManagerScreen() {
    var showImportDialog by remember { mutableStateOf(false) }
    if (showImportDialog)
        ImportModelsDialog { showImportDialog = false }
    val context = LocalContext.current


    val vm: ModelManagerViewModel = viewModel()
    val toolBarState = remember {
        ToolBarState()
    }
    Scaffold(topBar = {
        ModelManagerToolbar(
            state = toolBarState,
            onAdd = { showImportDialog = true },
            onSetLanguages = { vm.setLanguagesForSelectedModels(it) },
            onSelectAll = { vm.selectAll() },
            onSelectInvert = { vm.selectInvert() },
            onCancelSelect = { vm.selectedModels.clear() }
        )
    }) {
        ModelManagerScreenContent(
            Modifier
                .padding(it)
                .fillMaxSize(),
            vm = vm,
            toolBarState = toolBarState
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ModelManagerScreenContent(
    modifier: Modifier = Modifier,
    toolBarState: ToolBarState,
    vm: ModelManagerViewModel = viewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(key1 = vm) {
        vm.load()
    }


    var showAuditionDialog by remember { mutableStateOf<Pair<String, OfflineTtsConfig>?>(null) }
    if (showAuditionDialog != null) {
        AuditionDialog(
            onDismissRequest = { showAuditionDialog = null },
            offlineTtsConfig = showAuditionDialog!!.second,
            lang = showAuditionDialog!!.first
        )
    }

    var showModelEditDialog by remember { mutableStateOf<Model?>(null) }
    if (showModelEditDialog != null) {
        ModelEditDialog(
            onDismissRequest = { showModelEditDialog = null },
            model = showModelEditDialog!!,
            onSave = { ModelManager.updateModels(it) }
        )
    }

    val selectMode = vm.selectedModels.isNotEmpty()
    LaunchedEffect(key1 = vm.selectedModels.size) {
        toolBarState.selectedCount.value = vm.selectedModels.size
    }

    LaunchedEffect(key1 = toolBarState.selectedCount.value) {
        if (toolBarState.selectedCount.value == 0) vm.selectedModels.clear()
    }

    val reorderState = rememberReorderableLazyListState(onMove = { from, to ->
        vm.moveModel(from.index, to.index)
    })

    LazyColumn(modifier = modifier.reorderable(reorderState), state = reorderState.listState) {
        items(vm.models, { it.id }) { model ->
            val lang = remember(model.lang) { model.lang.toLocale().displayName }
            val enabled = TtsConfig.modelId.value == model.id
            val selected = vm.selectedModels.contains(model)
            ShadowReorderableItem(reorderableState = reorderState, key = model.id) {
                ModelItem(
                    modifier = Modifier
                        .animateItemPlacement()
                        .padding(4.dp),
                    reorderModifier = Modifier.detectReorder(reorderState),
                    name = model.name,
                    lang = lang,
                    enabled = enabled,
                    selected = selected,
                    onAudition = {
                        showAuditionDialog = model.lang to model.toOfflineTtsConfig()
                    },
                    onEdit = {
                        showModelEditDialog = model
                    },
                    onClick = {
                        if (selectMode) {
                            if (selected)
                                vm.selectedModels.remove(model)
                            else
                                vm.selectedModels.add(model)
                        } else
                            TtsConfig.modelId.value = model.id
                    },
                    onLongClick = {
                        if (selectMode) {
                            if (selected)
                                vm.selectedModels.remove(model)
                            else
                                vm.selectedModels.add(model)
                        } else
                            vm.selectedModels.add(model)
                    },
                    onDelete = {
                        vm.deleteModel(model)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ModelItem(
    modifier: Modifier,
    reorderModifier: Modifier = Modifier,
    name: String,
    lang: String,
    enabled: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onAudition: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    if (showDeleteConfirmDialog)
        ConfirmDeleteDialog(
            onDismissRequest = { showDeleteConfirmDialog = false }, name = name,
            desc = stringResource(R.string.delete_model_from_config_desc)
        ) {
            onDelete()
        }

    val color =
        if (enabled) MaterialTheme.colorScheme.primary else Color.Unspecified

    Card(
        modifier = modifier
            .clip(CardDefaults.shape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    view.performLongPress()
                    onLongClick()
                },
            )
            .semantics {
                this.stateDescription = if (enabled) context.getString(R.string.enabled)
                else context.getString(R.string.disabled)
            },
        colors = if (selected) CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                alpha = 0.5f
            )
        )
        else CardDefaults.elevatedCardColors(),
        border = if (selected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
    ) {
        Box(modifier = Modifier.padding(4.dp)) {
            Row {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .background(
                            if (enabled) color else Color.Unspecified,
                            shape = MaterialTheme.shapes.small
                        )
                        .width(4.dp)
                        .height(32.dp)

                )
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(start = 4.dp)
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleMedium,
                            color = color,
                            fontWeight = if (enabled) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                        )
                        Text(
                            text = lang,
                            style = MaterialTheme.typography.bodyMedium, color = color
                        )
                    }
                    Row {
                        IconButton(onClick = onAudition) {
                            Icon(
                                Icons.Default.Headset,
                                contentDescription = stringResource(id = R.string.audition),
                                tint = color
                            )
                        }

                        var showOptions by remember { mutableStateOf(false) }
                        IconButton(modifier = reorderModifier,
                            onClick = { showOptions = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = stringResource(id = R.string.more_options),
                                tint = color
                            )

                            DropdownMenu(
                                expanded = showOptions,
                                onDismissRequest = { showOptions = false }) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(id = R.string.edit)) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Edit, null)
                                    },
                                    onClick = {
                                        showOptions = false
                                        onEdit()
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text(stringResource(id = R.string.delete)) },
                                    leadingIcon = {
                                        Icon(Icons.Default.DeleteForever, null)
                                    },
                                    onClick = {
                                        showOptions = false
                                        showDeleteConfirmDialog = true
                                    }
                                )


                            }
                        }
                    }
                }
            }
        }
    }
}
