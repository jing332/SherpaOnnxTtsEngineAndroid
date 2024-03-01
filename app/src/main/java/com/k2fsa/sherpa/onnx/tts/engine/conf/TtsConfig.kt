package com.k2fsa.sherpa.onnx.tts.engine.conf

import com.funny.data_saver.core.DataSaverPreferences
import com.funny.data_saver.core.mutableDataSaverStateOf
import com.k2fsa.sherpa.onnx.tts.engine.App

object TtsConfig {
    private val dataSaverPref = DataSaverPreferences(App.instance.getSharedPreferences("tts", 0))

    val modelId = mutableDataSaverStateOf(
        dataSaverInterface = dataSaverPref,
        key = "modelId",
        initialValue = ""
    )

    val timeoutDestruction = mutableDataSaverStateOf(
        dataSaverInterface = dataSaverPref,
        key = "timeoutDestruction",
        initialValue = 3
    )

    val cacheSize = mutableDataSaverStateOf(
        dataSaverInterface = dataSaverPref,
        key = "cacheSize",
        initialValue = 3
    )

    val threadNum = mutableDataSaverStateOf(
        dataSaverInterface = dataSaverPref,
        key = "threadNum",
        initialValue = 2
    )

}