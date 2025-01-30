package com.revakovskyi.giphy.app.navigation

import com.revakovskyi.giphy.core.domain.gifs.Gif
import kotlinx.serialization.Serializable

sealed interface Routes {

    @Serializable
    data object Gifs : Routes

    @Serializable
    data class Original(val gif: Gif) : Routes

}
