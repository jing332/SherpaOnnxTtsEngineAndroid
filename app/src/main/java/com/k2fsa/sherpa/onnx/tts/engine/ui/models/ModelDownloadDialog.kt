package com.k2fsa.sherpa.onnx.tts.engine.ui.models

import android.content.Intent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.k2fsa.sherpa.onnx.tts.engine.R
import com.k2fsa.sherpa.onnx.tts.engine.service.DownloadModelService
import com.k2fsa.sherpa.onnx.tts.engine.service.DownloadModelService.Companion.EXTRA_FILE_NAME
import com.k2fsa.sherpa.onnx.tts.engine.ui.widgets.AppDialog
import com.k2fsa.sherpa.onnx.tts.engine.ui.widgets.LoadingContent
import com.k2fsa.sherpa.onnx.tts.engine.utils.clickableRipple

@Preview
@Composable
private fun PreviewModelDownloadDialog() {
    var show by remember { mutableStateOf(false) }

    ModelDownloadDialog(onDismissRequest = { show = false })
}

@Composable
fun ModelDownloadDialog(
    onDismissRequest: () -> Unit,
) {
    val vm: ModelDownloadViewModel = viewModel()
    val context = LocalContext.current

    LaunchedEffect(key1 = vm) {
        vm.load()
    }

    var showTips by remember { mutableStateOf(false) }
    if (showTips)
        TaskAddedTipsDialog(
            onDismissRequest = { showTips = false },
            title = stringResource(id = R.string.download_model)
        )


    AppDialog(onDismissRequest = onDismissRequest,
        title = { Text("Download model") }, content = {
            LoadingContent(isLoading = vm.modelList.isEmpty()) {
                LazyColumn {
                    items(vm.modelList) { asset ->
                        val checked =
                            remember(
                                vm.checkedModels.size,
                                asset
                            ) { vm.checkedModels.contains(asset) }
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickableRipple(/*role = Role.Checkbox*/) {
//                                    if (checked)
//                                        vm.checkedModels.remove(asset)
//                                    else vm.checkedModels.add(asset)
                                    onDismissRequest()
                                    context.startService(
                                        Intent(
                                            context,
                                            DownloadModelService::class.java
                                        ).apply {
                                            data = asset.browserDownloadUrl.toUri()
                                            putExtra(EXTRA_FILE_NAME, asset.name)
                                        }
                                    )
                                    showTips = true
                                }
                                .minimumInteractiveComponentSize()
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
//                            Checkbox(checked = checked, onCheckedChange = null)
                            Text(asset.name, style = MaterialTheme.typography.bodyMedium)

                        }
                    }
                }
            }
        }, buttons = {
            Row {
//                TextButton(onClick = { /*TODO*/ }) {
//                    Text(stringResource(id = android.R.string.ok))
//                }

                TextButton(onClick = onDismissRequest) {
                    Text(stringResource(id = android.R.string.cancel))
                }
            }
        })
}

