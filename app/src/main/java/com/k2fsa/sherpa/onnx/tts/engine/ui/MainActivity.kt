@file:OptIn(ExperimentalMaterial3Api::class)

package com.k2fsa.sherpa.onnx.tts.engine.ui

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddToPhotos
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.k2fsa.sherpa.onnx.tts.engine.R
import com.k2fsa.sherpa.onnx.tts.engine.ui.models.ImportModelsDialog
import com.k2fsa.sherpa.onnx.tts.engine.ui.models.ModelManagerScreen
import com.k2fsa.sherpa.onnx.tts.engine.ui.theme.SherpaOnnxTtsEngineTheme

const val TAG = "sherpa-onnx-tts-engine"

class MainActivity : ComponentActivity() {
    // TODO(fangjun): Save settings in ttsViewModel
    private val ttsViewModel: TtsViewModel by viewModels()

    private var mediaPlayer: MediaPlayer? = null
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        ModelManager.load(this)
//        TtsEngine.createTts(this)
        enableEdgeToEdge()
        setContent {
            SherpaOnnxTtsEngineTheme {
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
                    ModelManagerScreen(Modifier.padding(it))
                }
            }
        }
    }

    override fun onDestroy() {
        stopMediaPlayer()
        super.onDestroy()
    }

    private fun stopMediaPlayer() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}