package com.k2fsa.sherpa.onnx.tts.engine.ui.models

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drake.net.Get
import com.drake.net.Net
import com.k2fsa.sherpa.onnx.tts.engine.AppConst
import com.k2fsa.sherpa.onnx.tts.engine.GithubRelease
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ModelDownloadViewModel : ViewModel() {
    val modelList = mutableStateListOf<GithubRelease.Asset>()
    val checkedModels = mutableStateListOf<GithubRelease.Asset>()

    fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            val str =
                Get<String>("https://api.github.com/repos/k2-fsa/sherpa-onnx/releases/tags/tts-models").await()

            val release: GithubRelease = AppConst.jsonBuilder.decodeFromString(str)
            modelList.addAll(release.assets)
        }
    }
}