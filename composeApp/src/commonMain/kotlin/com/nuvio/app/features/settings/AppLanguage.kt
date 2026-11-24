package com.nuvio.app.features.settings

import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.lang_english
import nuvio.composeapp.generated.resources.lang_spanish
import nuvio.composeapp.generated.resources.settings_appearance_app_language_device
import org.jetbrains.compose.resources.StringResource

enum class AppLanguage(
    val code: String,
    val labelRes: StringResource,
) {
    DEVICE("device", Res.string.settings_appearance_app_language_device),
    ENGLISH("en", Res.string.lang_english),
    SPANISH("es", Res.string.lang_spanish),
    ;

    // Español e inglés se escriben de izquierda a derecha.
    fun isRightToLeft(): Boolean = false

    companion object {
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: DEVICE
    }
}
