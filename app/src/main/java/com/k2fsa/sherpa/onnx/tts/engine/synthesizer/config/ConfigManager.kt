package com.k2fsa.sherpa.onnx.tts.engine.synthesizer.config

import android.util.Log
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import com.k2fsa.sherpa.onnx.tts.engine.synthesizer.ModelConstants.configPath
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.encodeToString
import java.io.File

object ConfigManager {
    const val TAG = "ConfigManager"

    private var mConfig: ModelConfig? = null

    val config
        get() = mConfig ?: readConfig().run { mConfig!! }

    fun updateConfig(config: ModelConfig) {
        mConfig = config
        write(config = config)

        _configFlow.tryEmit(mConfig!!)
    }

    fun readConfig() {
        mConfig = read()
        _configFlow.tryEmit(mConfig!!)
    }

    private val _configFlow by lazy {
        MutableStateFlow(config)
    }

    val configFlow: Flow<ModelConfig>
        get() = _configFlow

    private fun read(path: String = configPath): ModelConfig {
        val file = File(path)

        try {
            file.inputStream().use {
                return Yaml.default.decodeFromStream<ModelConfig>(it)
            }
        } catch (e: Exception) {
            Log.e(TAG, "readConfig: ", e)
            write(config = ModelConfig())
            return ModelConfig()
        }
    }

    private fun write(path: String = configPath, config: ModelConfig) {
        val file = File(path)

        file.writeText(Yaml.default.encodeToString(config))
    }
}