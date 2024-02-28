package com.k2fsa.sherpa.onnx.tts.engine.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.k2fsa.sherpa.onnx.tts.engine.R
import com.k2fsa.sherpa.onnx.tts.engine.conf.TtsConfig

@Composable
fun SettingsScreen() {
    Scaffold { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            DividerPreference {
                Text(stringResource(id = R.string.engine_cache))
            }

            var timeout by remember { TtsConfig.timeoutDestruction }
            val timeoutStr = stringResource(id = R.string.minute_format, timeout)
            SliderPreference(
                valueRange = 1f..60f,
                steps = 60,
                title = { Text(stringResource(R.string.timeout_destruction)) },
                subTitle = { Text(stringResource(R.string.timeout_destruction_summary))},
                value = timeout.toFloat(),
                onValueChange = { timeout = it.toInt() },
                label = timeoutStr
            )
        }
    }
}
