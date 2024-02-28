package com.k2fsa.sherpa.onnx.tts.engine.utils

import java.util.Locale

object LocaleUtils {

}

fun Locale.equalsIso3(
    iso3Lang: String,
    iso3Country: String = "",
    iso3Variant: String = ""
): Boolean {
    val c = try {
        this.isO3Country
    } catch (e: Exception) {
        ""
    }

    val v = try {
        this.variant
    } catch (e: Exception) {
        ""
    }

    return this.isO3Language == iso3Lang && c == iso3Country && v == iso3Variant
}

fun String.toLocale(): Locale {
    val parts = split("-")

    return when (parts.size) {
        1 -> Locale(parts[0])
        2 -> Locale(parts[0], parts[1])
        else -> Locale(this)
    }
}