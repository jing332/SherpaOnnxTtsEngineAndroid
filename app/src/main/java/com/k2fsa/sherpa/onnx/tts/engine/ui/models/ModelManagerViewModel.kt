package com.k2fsa.sherpa.onnx.tts.engine.ui.models

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.k2fsa.sherpa.onnx.tts.engine.synthesizer.ModelManager
import com.k2fsa.sherpa.onnx.tts.engine.synthesizer.config.Model
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ModelManagerViewModel : ViewModel() {
    val models = mutableStateOf<List<Model>>(emptyList())

    fun load() {
        viewModelScope.launch {
            ModelManager.modelsFlow.onEach {
                println("collect: ${it.hashCode()}")
                models.value = it
            }.collectLatest{

            }
        }
    }
}