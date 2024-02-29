package com.k2fsa.sherpa.onnx.tts.engine

import com.charleskorn.kaml.SingleLineStringStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

object AppConst {
    val yaml = Yaml(
        configuration = YamlConfiguration(
            strictMode = false,
            singleLineStringStyle = SingleLineStringStyle.PlainExceptAmbiguous
        )
    )

    @OptIn(ExperimentalSerializationApi::class)
    val jsonBuilder by lazy {
        Json {
            allowStructuredMapKeys = true
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
            explicitNulls = false //忽略为null的字段
            allowStructuredMapKeys = true
        }
    }
}