package com.k2fsa.sherpa.onnx.tts.engine.synthesizer.config

import kotlinx.serialization.Serializable

@Serializable
data class ModelConfig(
    val models: List<Model> = emptyList()
)