package com.k2fsa.sherpa.onnx.tts.engine.ui.voices

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.k2fsa.sherpa.onnx.tts.engine.R
import com.k2fsa.sherpa.onnx.tts.engine.conf.TtsConfig
import com.k2fsa.sherpa.onnx.tts.engine.synthesizer.config.Voice
import com.k2fsa.sherpa.onnx.tts.engine.ui.AuditionDialog
import com.k2fsa.sherpa.onnx.tts.engine.ui.ConfirmDeleteDialog
import com.k2fsa.sherpa.onnx.tts.engine.ui.ErrorHandler
import com.k2fsa.sherpa.onnx.tts.engine.ui.ShadowReorderableItem
import com.k2fsa.sherpa.onnx.tts.engine.ui.widgets.AppSelectionToolBar
import com.k2fsa.sherpa.onnx.tts.engine.ui.widgets.SelectableCard
import com.k2fsa.sherpa.onnx.tts.engine.ui.widgets.SelectionToolBarState
import com.k2fsa.sherpa.onnx.tts.engine.ui.widgets.TextFieldDialog
import com.k2fsa.sherpa.onnx.tts.engine.ui.widgets.VerticalBar
import com.k2fsa.sherpa.onnx.tts.engine.utils.clickableRipple
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun VoiceManagerScreen() {
    val vm: VoiceManagerViewModel = viewModel()

    var showAddVoiceDialog by remember { mutableStateOf<Voice?>(null) }
    if (showAddVoiceDialog != null) {
        AddVoiceDialog(
            onDismissRequest = { showAddVoiceDialog = null },
            initialVoice = showAddVoiceDialog!!,
            onConfirm = {
                showAddVoiceDialog = null
                vm.addVoice(it)
            }
        )
    }

    var showEditNameDialog by remember { mutableStateOf<Voice?>(null) }
    if (showEditNameDialog != null) {
        val voice = showEditNameDialog!!
        TextFieldDialog(
            title = stringResource(id = R.string.display_name),
            initialText = voice.name,
            onDismissRequest = { showEditNameDialog = null }) {
            showEditNameDialog = null
            vm.updateVoice(voice.copy(name = it))
        }
    }


    var showAudition by remember { mutableStateOf<Voice?>(null) }
    if (showAudition != null) {
        AuditionDialog(onDismissRequest = { showAudition = null }, voice = showAudition!!)
    }

    Scaffold(topBar = {
        val selectionState = remember {
            SelectionToolBarState(
                onSelectAll = { /*TODO*/ },
                onSelectInvert = { /*TODO*/ },
                onSelectClear = { /*TODO*/ }
            )
        }
        AppSelectionToolBar(state = selectionState, mainBar = {
            TopAppBar(title = { Text(stringResource(id = R.string.app_name)) }, actions = {
                IconButton(onClick = {
                    showAddVoiceDialog = Voice.EMPTY
                }) {
                    Icon(Icons.Default.Add, stringResource(id = R.string.add_voice))
                }

                var showSortOptions by rememberSaveable { mutableStateOf(false) }
                IconButton(onClick = { showSortOptions = true }) {
                    Icon(Icons.Default.SortByAlpha, stringResource(id = R.string.sort))

                    DropdownMenu(
                        expanded = showSortOptions,
                        onDismissRequest = { showSortOptions = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sort_by_name)) },
                            onClick = {
                                showSortOptions = false
                                vm.sortByName()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sort_by_model)) },
                            onClick = {
                                showSortOptions = false
                                vm.sortByModel()
                            }
                        )
                    }
                }
            })
        }) {

        }
    }) { paddingValues ->
        LaunchedEffect(key1 = vm) {
            vm.load()
        }
        ErrorHandler(vm = vm)

        val reorderState =
            rememberReorderableLazyListState(listState = vm.listState, onMove = { from, to ->
                vm.move(from.index, to.index)
            })

        if (vm.voices.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(id = R.string.no_voices_tips),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        LazyColumn(
            Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .reorderable(reorderState),
            state = reorderState.listState
        ) {
            items(vm.voices, key = { it.toString() }) { voice ->
                ShadowReorderableItem(reorderableState = reorderState, key = voice.toString()) {
                    val enabled = voice.contains(TtsConfig.voice.value)
                    Item(
                        modifier = Modifier
                            .animateItemPlacement()
                            .padding(4.dp),
                        reorderModifier = Modifier.detectReorder(reorderState),
                        enabled = enabled,
                        selected = vm.isSelected(voice),
                        name = voice.name,
                        model = voice.model,
                        id = voice.id.toString(),
                        onClick = {
                            if (enabled) {

                            } else {
                                TtsConfig.voice.value = voice
                            }
                        },
                        onCopy = { showAddVoiceDialog = voice },
                        onDelete = { vm.delete(voice) },
                        onAudition = { showAudition = voice },
                        onEditName = { showEditNameDialog = voice }
                    )
                }
            }
        }
    }
}

@Composable
private fun Item(
    modifier: Modifier,
    reorderModifier: Modifier,
    enabled: Boolean,
    selected: Boolean,

    id: String,
    name: String,
    model: String,

    onClick: () -> Unit,
    onEditName: () -> Unit,
    onAudition: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    if (showDeleteDialog) {
        ConfirmDeleteDialog(onDismissRequest = { showDeleteDialog = false }, name = name) {
            onDelete()
        }
    }


    val context = LocalContext.current
    val color =
        if (enabled) MaterialTheme.colorScheme.primary else Color.Unspecified
    val tint =
        if (enabled) MaterialTheme.colorScheme.primary else LocalContentColor.current

    SelectableCard(
        modifier
            .clip(CardDefaults.shape)
            .clickableRipple { onClick() },
        selected = selected
    ) {
        Row(Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
            VerticalBar(enabled = enabled)

            Column(
                Modifier
                    .padding(start = 4.dp)
                    .weight(1f)
            ) {
                Text(text = name, style = MaterialTheme.typography.titleMedium, color = color)
                Row {
                    if (id != "0")
                        Text(
                            modifier = Modifier.padding(end = 4.dp),
                            text = id,
                            style = MaterialTheme.typography.bodyMedium,
                            color = color
                        )

                    Text(text = model, style = MaterialTheme.typography.bodyMedium, color = color)
                }
            }

            Row {
                IconButton(onClick = onAudition) {
                    Icon(Icons.Default.Headset, stringResource(id = R.string.audition), tint = tint)
                }

                var showOptions by rememberSaveable { mutableStateOf(false) }
                IconButton(modifier = reorderModifier, onClick = { showOptions = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        stringResource(id = R.string.more_options),
                        tint = tint
                    )

                    DropdownMenu(
                        expanded = showOptions,
                        onDismissRequest = { showOptions = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(id = R.string.edit_name)) },
                            onClick = onEditName,
                            leadingIcon = {
                                Icon(Icons.Default.EditNote, null)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text(stringResource(id = android.R.string.copy)) },
                            onClick = onCopy,
                            leadingIcon = {
                                Icon(Icons.Default.ContentCopy, null)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text(stringResource(id = R.string.delete)) },
                            onClick = { showDeleteDialog = true },
                            leadingIcon = {
                                Icon(Icons.Default.DeleteForever, null)
                            }
                        )
                    }
                }
            }
        }
    }
}