package com.revakovskyi.giphy.app.navigation

import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavType
import com.revakovskyi.giphy.core.domain.gifs.Gif
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object CustomNavType {

    val PetType = object : NavType<Gif>(isNullableAllowed = false) {
        override fun get(bundle: Bundle, key: String): Gif? {
            return Json.decodeFromString(bundle.getString(key) ?: return null)
        }

        override fun put(bundle: Bundle, key: String, value: Gif) {
            bundle.putString(key, Json.encodeToString(value))
        }

        override fun parseValue(value: String): Gif {
            return Json.decodeFromString(Uri.decode(value))
        }

        override fun serializeAsValue(value: Gif): String {
            return Uri.encode(Json.encodeToString(value))
        }

    }

}
