package com.k2fsa.sherpa.onnx.tts.engine

import android.app.Application
import com.k2fsa.sherpa.onnx.tts.engine.synthesizer.config.SampleTextConfig

val app by lazy { App.instance }

class App : Application() {
    companion object {
        lateinit var instance: App
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

}