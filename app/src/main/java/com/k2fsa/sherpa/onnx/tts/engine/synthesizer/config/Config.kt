package com.k2fsa.sherpa.onnx.tts.engine.synthesizer.config

import kotlinx.serialization.Serializable

@Serializable
data class Config(
    val models: List<Model> = emptyList(),
    val speakers: List<Voice> = emptyList()
)