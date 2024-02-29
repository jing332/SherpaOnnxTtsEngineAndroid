package com.k2fsa.sherpa.onnx.tts.engine.ui.models

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.k2fsa.sherpa.onnx.tts.engine.synthesizer.ModelManager
import com.k2fsa.sherpa.onnx.tts.engine.synthesizer.config.Model
import kotlinx.coroutines.launch
import java.util.Collections

class ModelManagerViewModel : ViewModel() {
    internal val models = mutableStateListOf<Model>()
    internal val selectedModels = mutableStateListOf<Model>()

    fun load() {
        viewModelScope.launch {
            ModelManager.load()
            ModelManager.modelsFlow.collect {
                println("collect: ${it.hashCode()}")
                models.clear()
                models.addAll(it)
            }
        }
    }

    fun moveModel(from: Int, to: Int) {
        val list = ModelManager.models().toMutableList()
        Collections.swap(list, from, to)
        ModelManager.updateModels(list)
    }

    fun deleteModel(model: Model) {
        ModelManager.removeModel(model)
    }

    fun setLanguagesForSelectedModels(lang: String) {
        val list = ModelManager.models().toMutableList()
        list.forEachIndexed { index, model ->
            if (selectedModels.find { it.id == model.id } != null) {
                list[index] = model.copy(lang = lang)
            }
        }
        ModelManager.updateModels(list)
    }

    fun selectAll() {
        selectedModels.clear()
        selectedModels.addAll(models)
    }

    fun selectInvert() {
        ModelManager.models().filter { !selectedModels.contains(it) }.let {
            selectedModels.clear()
            selectedModels.addAll(it)
        }
    }
}