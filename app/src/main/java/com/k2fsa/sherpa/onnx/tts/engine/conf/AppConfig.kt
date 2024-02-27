package com.k2fsa.sherpa.onnx.tts.engine.conf

import com.funny.data_saver.core.DataSaverPreferences
import com.funny.data_saver.core.mutableDataSaverStateOf
import com.k2fsa.sherpa.onnx.tts.engine.R
import com.k2fsa.sherpa.onnx.tts.engine.App

object AppConfig {
    private val dataSaverPref = DataSaverPreferences(App.instance.getSharedPreferences("app", 0))

    val sampleText = mutableDataSaverStateOf(
        dataSaverInterface = dataSaverPref,
        key = "sampleText",
        initialValue = App.instance.getString(R.string.sample_text1)
    )

}