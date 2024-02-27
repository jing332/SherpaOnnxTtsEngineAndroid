package com.k2fsa.sherpa.onnx.tts.engine.synthesizer

import com.k2fsa.sherpa.onnx.tts.engine.app

object ModelConstants {
    var modelPath: String =
        app.getExternalFilesDir("model")!!.absolutePath

    var configPath: String = "$modelPath/config.yaml"
}