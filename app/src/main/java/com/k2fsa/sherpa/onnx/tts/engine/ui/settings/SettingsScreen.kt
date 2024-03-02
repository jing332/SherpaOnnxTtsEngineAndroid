package com.k2fsa.sherpa.onnx.tts.engine.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.k2fsa.sherpa.onnx.tts.engine.R
import com.k2fsa.sherpa.onnx.tts.engine.conf.TtsConfig
import com.k2fsa.sherpa.onnx.tts.engine.ui.sampletext.SampleTextManagerActivity
import com.k2fsa.sherpa.onnx.tts.engine.utils.startActivity

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    Scaffold { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            DividerPreference { Text("TTS") }
            BasePreferenceWidget(
                onClick = { context.startActivity(SampleTextManagerActivity::class.java) },
                title = { Text(stringResource(R.string.sample_text)) },
                subTitle = { Text(stringResource(R.string.sample_text_summary)) },
                icon = { Icon(Icons.Default.TextFields, null) }
            )

            DividerPreference {
                Text(stringResource(id = R.string.engine))
            }

            var timeout by remember { TtsConfig.timeoutDestruction }
            val timeoutStr = stringResource(id = R.string.minute_format, timeout)
            SliderPreference(
                icon = { Icon(Icons.Default.LinearScale, null) },
                valueRange = 1f..60f,
                title = { Text(stringResource(R.string.timeout_destruction)) },
                subTitle = { Text(stringResource(R.string.timeout_destruction_summary)) },
                value = timeout.toFloat(),
                onValueChange = { timeout = it.toInt() },
                label = timeoutStr
            )

            var cacheSize by remember { TtsConfig.cacheSize }
            val cacheSizeStr = cacheSize.toString()
            SliderPreference(
                icon = { Icon(Icons.Default.Height, null) },
                valueRange = 1f..10f,
                title = { Text(stringResource(R.string.cache_size)) },
                subTitle = { Text(stringResource(R.string.cache_size_summary)) },
                value = cacheSize.toFloat(),
                onValueChange = { cacheSize = it.toInt() },
                label = cacheSizeStr
            )

            var threadNum by remember { TtsConfig.threadNum }
            val threadNumStr = threadNum.toString()
            SliderPreference(
                icon = { Icon(Icons.Default.Workspaces, null) },
                valueRange = 1f..8f,
                title = { Text(stringResource(R.string.thread_num)) },
                subTitle = { Text(stringResource(R.string.thread_num_summary)) },
                value = threadNum.toFloat(),
                onValueChange = { threadNum = it.toInt() },
                label = threadNumStr
            )
        }
    }
}
