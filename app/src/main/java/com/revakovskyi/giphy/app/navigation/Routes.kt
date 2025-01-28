package com.revakovskyi.giphy.app.navigation

import kotlinx.serialization.Serializable

sealed interface Routes {

    @Serializable
    data object Gifs : Routes

    @Serializable
    data class Original(val gifId: String) : Routes

}
