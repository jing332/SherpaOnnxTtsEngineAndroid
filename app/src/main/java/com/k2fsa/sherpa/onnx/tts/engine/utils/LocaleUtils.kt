package com.k2fsa.sherpa.onnx.tts.engine.utils

import java.util.Locale

object LocaleUtils {

}

val Locale.mustIso3Language: String
    get() = try {
        this.isO3Language
    } catch (e: Exception) {
        ""
    }

val Locale.mustIso3Country: String
    get() = try {
        this.isO3Country
    } catch (e: Exception) {
        ""
    }

val Locale.mustVariant: String
    get() = try {
        this.variant
    } catch (e: Exception) {
        ""
    }

fun Locale.toIso3Code(): String {
    val lang = mustIso3Language
    val country = mustIso3Country
    val variant = mustVariant

    return when {
        lang.isNotEmpty() && country.isNotEmpty() && variant.isNotEmpty() -> "$lang-$country-$variant"
        lang.isNotEmpty() && country.isNotEmpty() -> "$lang-$country"
        lang.isNotEmpty() -> lang
        else -> ""
    }
}

fun Locale.equalsIso3(
    iso3Lang: String,
    iso3Country: String = "",
    iso3Variant: String = ""
): Boolean {
    return this.mustIso3Language == iso3Lang &&
            this.mustIso3Country == iso3Country &&
            this.mustVariant == iso3Variant
}

fun String.toLocale(): Locale {
    val parts = split("-")

    return when (parts.size) {
        1 -> Locale(parts[0])
        2 -> Locale(parts[0], parts[1])
        else -> Locale(this)
    }
}