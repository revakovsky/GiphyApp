package com.revakovskyi.giphy.core.domain.gifs

data class Gif(
    val id: String,
    val queryId: Long,
    val urlSmallImage: String,
    val urlOriginalImage: String,
)
