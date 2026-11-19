package com.nuvio.app.features.settings

import nuvio.composeapp.generated.resources.Res
<<<<<<< GRACIE
||||||| NUVIO-0.6.11-beta
import nuvio.composeapp.generated.resources.lang_arabic
import nuvio.composeapp.generated.resources.lang_bengali
import nuvio.composeapp.generated.resources.lang_bulgarian
import nuvio.composeapp.generated.resources.lang_croatian
import nuvio.composeapp.generated.resources.lang_czech
=======
import nuvio.composeapp.generated.resources.lang_arabic
import nuvio.composeapp.generated.resources.lang_bengali
import nuvio.composeapp.generated.resources.lang_bulgarian
import nuvio.composeapp.generated.resources.lang_croatian
import nuvio.composeapp.generated.resources.lang_czech
import nuvio.composeapp.generated.resources.lang_dutch
>>>>>>> NUVIO-1.1.0-beta.2
import nuvio.composeapp.generated.resources.lang_english
import nuvio.composeapp.generated.resources.lang_spanish
<<<<<<< GRACIE
||||||| NUVIO-0.6.11-beta
import nuvio.composeapp.generated.resources.lang_turkish
import nuvio.composeapp.generated.resources.lang_tamil
import nuvio.composeapp.generated.resources.lang_urdu
import nuvio.composeapp.generated.resources.lang_norwegian
import nuvio.composeapp.generated.resources.lang_dutch
import nuvio.composeapp.generated.resources.lang_japanese
import nuvio.composeapp.generated.resources.lang_vietnamese
=======
import nuvio.composeapp.generated.resources.lang_turkish
import nuvio.composeapp.generated.resources.lang_tamil
import nuvio.composeapp.generated.resources.lang_ukrainian
import nuvio.composeapp.generated.resources.lang_urdu
import nuvio.composeapp.generated.resources.lang_norwegian
import nuvio.composeapp.generated.resources.lang_japanese
import nuvio.composeapp.generated.resources.lang_vietnamese
import nuvio.composeapp.generated.resources.lang_chinese
>>>>>>> NUVIO-1.1.0-beta.2
import nuvio.composeapp.generated.resources.settings_appearance_app_language_device
import org.jetbrains.compose.resources.StringResource

enum class AppLanguage(
    val code: String,
    val labelRes: StringResource,
) {
    DEVICE("device", Res.string.settings_appearance_app_language_device),
<<<<<<< GRACIE
||||||| NUVIO-0.6.11-beta
    ARABIC("ar", Res.string.lang_arabic),
    BENGALI("bn", Res.string.lang_bengali),
    BULGARIAN("bg", Res.string.lang_bulgarian),
    CROATIAN("hr", Res.string.lang_croatian),
    CZECH("cs", Res.string.lang_czech),
=======
    ARABIC("ar", Res.string.lang_arabic),
    BENGALI("bn", Res.string.lang_bengali),
    BULGARIAN("bg", Res.string.lang_bulgarian),
    CROATIAN("hr", Res.string.lang_croatian),
    CZECH("cs", Res.string.lang_czech),
    DUTCH("nl", Res.string.lang_dutch),
>>>>>>> NUVIO-1.1.0-beta.2
    ENGLISH("en", Res.string.lang_english),
    SPANISH("es", Res.string.lang_spanish),
<<<<<<< GRACIE
||||||| NUVIO-0.6.11-beta
    TAMIL("ta", Res.string.lang_tamil),
    TURKISH("tr", Res.string.lang_turkish),
    URDU("ur", Res.string.lang_urdu),
    NORWEGIAN("nb", Res.string.lang_norwegian),
    DUTCH("nl", Res.string.lang_dutch),
    JAPANESE("ja", Res.string.lang_japanese),
    VIETNAMESE("vi", Res.string.lang_vietnamese),
=======
    TAMIL("ta", Res.string.lang_tamil),
    TURKISH("tr", Res.string.lang_turkish),
    UKRAINIAN("uk", Res.string.lang_ukrainian),
    URDU("ur", Res.string.lang_urdu),
    NORWEGIAN("nb", Res.string.lang_norwegian),
    JAPANESE("ja", Res.string.lang_japanese),
    VIETNAMESE("vi", Res.string.lang_vietnamese),
    CHINESE("zh", Res.string.lang_chinese),
>>>>>>> NUVIO-1.1.0-beta.2
    ;

    // Español e inglés se escriben de izquierda a derecha.
    fun isRightToLeft(): Boolean = false

    companion object {
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: DEVICE
    }
}
