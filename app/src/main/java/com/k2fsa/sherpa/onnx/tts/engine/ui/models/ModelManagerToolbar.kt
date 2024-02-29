package com.k2fsa.sherpa.onnx.tts.engine.ui.models

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddToPhotos
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.k2fsa.sherpa.onnx.tts.engine.R
import com.k2fsa.sherpa.onnx.tts.engine.ui.sampletext.SampleTextManagerActivity
import com.k2fsa.sherpa.onnx.tts.engine.ui.widgets.AppDialog
import com.k2fsa.sherpa.onnx.tts.engine.ui.widgets.AppTooltip
import com.k2fsa.sherpa.onnx.tts.engine.utils.startActivity


class ToolBarState() {
    internal val selectedCount: MutableState<Int> = mutableIntStateOf(0)

    internal val closeSearch: MutableState<Boolean> = mutableStateOf(false)
    fun close() {
        closeSearch.value = true
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionToolBar(
    modifier: Modifier,
    selectedCount: Int,
    onSelectAll: () -> Unit,
    onSelectInvert: () -> Unit,
    onCancelSelect: () -> Unit,
    onSetLanguages: (language: String) -> Unit,
) {
    BackHandler(selectedCount > 0) {
        onCancelSelect()
    }

    var showLanguageDialog by remember { mutableStateOf(false) }
    if (showLanguageDialog) {
        var text by remember { mutableStateOf("") }
        AppDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(id = R.string.language)) },
            content = {
                LanguageTextField(modifier = Modifier.fillMaxWidth(), language = text) {
                    text = it
                }
            },
            buttons = {
                Row {
                    TextButton(onClick = { showLanguageDialog = false }) {
                        Text(stringResource(id = android.R.string.cancel))
                    }
                    TextButton(
                        enabled = text.isNotBlank(),
                        onClick = {
                            showLanguageDialog = false
                            onSetLanguages(text)
                        }) {
                        Text(stringResource(id = android.R.string.ok))
                    }

                }
            }
        )
    }

    TopAppBar(title = { Text(text = "$selectedCount") }, modifier = modifier,
        navigationIcon = {
            IconButton(onClick = onCancelSelect) {
                Icon(Icons.Default.Close, stringResource(R.string.cancel_select))
            }
        },
        actions = {
            AppTooltip(tooltip = stringResource(android.R.string.selectAll)) {
                IconButton(onClick = onSelectAll) {
                    Icon(Icons.Default.SelectAll, it)
                }
            }

            AppTooltip(tooltip = stringResource(id = R.string.invert_select)) {
                IconButton(onClick = onSelectInvert) {
                    Icon(Icons.Default.Deselect, it)
                }
            }

            var showOptions by remember { mutableStateOf(false) }
            IconButton(onClick = { showOptions = true }) {
                Icon(Icons.Default.MoreVert, stringResource(R.string.more_options))

                DropdownMenu(expanded = showOptions, onDismissRequest = { showOptions = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(id = R.string.change_language)) },
                        onClick = {
                            showLanguageDialog = true
                            showOptions = false
                        }
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainToolBar(modifier: Modifier, onAddModels: () -> Unit, onImportModels: () -> Unit) {
    val context = LocalContext.current
    TopAppBar(
        modifier = modifier,
        title = { Text(stringResource(id = R.string.app_name)) },
        actions = {
            IconButton(onClick = {
                context.startActivity(SampleTextManagerActivity::class.java)
            }) {
                Icon(Icons.Default.TextFields, stringResource(R.string.sample_text))
            }

            var showOptions by remember { mutableStateOf(false) }
            IconButton(onClick = { showOptions = true }) {
                Icon(Icons.Default.MoreVert, stringResource(id = R.string.more_options))
                DropdownMenu(expanded = showOptions, onDismissRequest = { showOptions = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.add_models)) },
                        onClick = onAddModels,
                        leadingIcon = {
                            Icon(Icons.Default.AddToPhotos, null)
                        }
                    )

                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.import_model_package)) },
                        onClick = onImportModels,
                        leadingIcon = {
                            Icon(Icons.Default.Archive, null)
                        }
                    )
                }
            }

        }
    )
}


@Composable
fun ModelManagerToolbar(
    state: ToolBarState,
    onAddModels: () -> Unit,
    onImportModels: () -> Unit,
    onSetLanguages: (language: String) -> Unit,
    onSelectAll: () -> Unit,
    onSelectInvert: () -> Unit,
    onCancelSelect: () -> Unit
) {
    Crossfade(targetState = state.selectedCount.value > 0, label = "") { selectMode ->
        if (selectMode) {
            SelectionToolBar(
                modifier = Modifier,
                selectedCount = state.selectedCount.value,
                onCancelSelect = onCancelSelect,
                onSelectAll = onSelectAll,
                onSelectInvert = onSelectInvert,
                onSetLanguages = onSetLanguages
            )
        } else {
            MainToolBar(
                modifier = Modifier,
                onAddModels = onAddModels,
                onImportModels = onImportModels
            )
        }

    }
}