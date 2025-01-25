package com.revakovskyi.giphy.app.navigation

import kotlinx.serialization.Serializable

sealed interface Destinations {

    @Serializable
    data object Gifs : Destinations

    @Serializable
    data class GifDetail(val url: String) : Destinations

}
