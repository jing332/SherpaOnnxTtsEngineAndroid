package com.k2fsa.sherpa.onnx.tts.engine.util

fun FloatArray.toByteArray(): ByteArray {
    // byteArray is actually a ShortArray
    val byteArray = ByteArray(this.size * 2)
    for (i in this.indices) {
        val sample = (this[i] * 32767).toInt()
        byteArray[2 * i] = sample.toByte()
        byteArray[2 * i + 1] = (sample shr 8).toByte()
    }
    return byteArray
}