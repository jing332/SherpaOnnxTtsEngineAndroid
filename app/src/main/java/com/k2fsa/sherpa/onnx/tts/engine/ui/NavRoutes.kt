package com.k2fsa.sherpa.onnx.tts.engine.ui

import com.k2fsa.sherpa.onnx.tts.engine.R

sealed class NavRoutes(val id: String, val strId: Int) {
    data object ModelManager : NavRoutes("model_manager", R.string.model_manager)
    data object Settings : NavRoutes("settings", R.string.settings)
}