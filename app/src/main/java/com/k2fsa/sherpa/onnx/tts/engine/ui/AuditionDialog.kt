package com.k2fsa.sherpa.onnx.tts.engine.ui

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.k2fsa.sherpa.onnx.OfflineTtsConfig
import com.k2fsa.sherpa.onnx.tts.engine.conf.AppConfig
import com.k2fsa.sherpa.onnx.tts.engine.R
import com.k2fsa.sherpa.onnx.tts.engine.synthesizer.SynthesizerManager
import com.k2fsa.sherpa.onnx.tts.engine.util.PcmAudioPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private fun getAudioTrack(sampleRate: Int): AudioTrack {
    val bufLength = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_FLOAT
    )
    Log.i(TAG, "sampleRate: ${sampleRate}, buffLength: ${bufLength}")

    val attr = AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .build()

    val format = AudioFormat.Builder()
        .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
        .setSampleRate(sampleRate)
        .build()

    return AudioTrack(
        attr, format, bufLength, AudioTrack.MODE_STREAM,
        AudioManager.AUDIO_SESSION_ID_GENERATE
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditionDialog(
    onDismissRequest: () -> Unit,
    text: String = AppConfig.sampleText.value,
    offlineTtsConfig: OfflineTtsConfig
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val audioPlayer = remember {
        PcmAudioPlayer()
    }

    LaunchedEffect(key1 = Unit) {
        val tts = SynthesizerManager.getTTS(offlineTtsConfig)
        val track = getAudioTrack(tts.sampleRate())
        track.play()
        scope.launch(Dispatchers.IO) {
            tts.generateWithCallback(text = text) {
                println("write to track: ${it.size}")
                track.write(it, 0, it.size, AudioTrack.WRITE_BLOCKING)
            }
            withContext(Dispatchers.Main) { onDismissRequest() }
        }
    }

    DisposableEffect(key1 = Unit) {
        onDispose {
            audioPlayer.release()
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = {
                onDismissRequest()
            }) {
                Text(stringResource(id = android.R.string.cancel))
            }
        },
        title = {
            Column {
                Text(
                    text = stringResource(id = R.string.audition),
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = offlineTtsConfig.model.vits.model.split("/").last(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        text = {
            Text(text = text, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    )
}