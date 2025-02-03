package com.revakovskyi.giphy.core.domain.gifs

import kotlinx.serialization.Serializable

@Serializable
data class Gif(
    val id: String,
    val queryId: Long,
    val urlSmallImage: String,
    val urlOriginalImage: String,
    val position: Int,
)
