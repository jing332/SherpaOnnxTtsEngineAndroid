package com.k2fsa.sherpa.onnx.tts.engine

import com.charleskorn.kaml.SingleLineStringStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration

object AppConst {
    val yaml = Yaml(
        configuration = YamlConfiguration(
            strictMode = false,
            singleLineStringStyle = SingleLineStringStyle.PlainExceptAmbiguous
        )
    )
}