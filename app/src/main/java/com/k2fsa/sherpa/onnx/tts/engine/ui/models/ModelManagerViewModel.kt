package com.k2fsa.sherpa.onnx.tts.engine.ui.models

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.k2fsa.sherpa.onnx.tts.engine.synthesizer.config.Model

class ModelManagerViewModel : ViewModel() {
    val models = mutableStateListOf<Model>()

    fun load() {
//        viewModelScope.launch {
//            ModelConfigManager.configFlow.collect {
//                models.clear()
//                models.addAll(it.models)
//            }
//        }
    }
}