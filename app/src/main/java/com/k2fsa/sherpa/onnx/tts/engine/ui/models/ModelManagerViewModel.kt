package com.k2fsa.sherpa.onnx.tts.engine.ui.models

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.k2fsa.sherpa.onnx.tts.engine.synthesizer.ModelManager
import com.k2fsa.sherpa.onnx.tts.engine.synthesizer.config.Model
import kotlinx.coroutines.launch
import java.util.Collections

class ModelManagerViewModel : ViewModel() {
    val models = mutableStateOf<List<Model>>(emptyList())

    fun load() {
        viewModelScope.launch {
            ModelManager.load()
            ModelManager.modelsFlow.collect {
                println("collect: ${it.hashCode()}")
                models.value = it
            }
        }
    }

    fun moveModel(from: Int, to: Int) {
        val list = ModelManager.models().toMutableList()
        Collections.swap(list, from, to)
        ModelManager.updateModels(list)
    }
}