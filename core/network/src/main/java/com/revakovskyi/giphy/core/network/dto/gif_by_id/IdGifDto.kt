package com.revakovskyi.giphy.core.network.dto.gif_by_id

import com.google.gson.annotations.SerializedName

data class IdGifDto(
    @SerializedName("data")
    val gifInfo: GifInfo,
)

data class GifInfo(
    @SerializedName("id")
    val id: String,
    @SerializedName("images")
    val images: Images,
)

data class Images(
    @SerializedName("original")
    val original: Original,
)

data class Original(
    @SerializedName("url")
    val url: String,
)
