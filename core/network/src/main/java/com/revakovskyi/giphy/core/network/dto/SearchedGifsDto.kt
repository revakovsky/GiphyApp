package com.revakovskyi.giphy.core.network.dto

import com.google.gson.annotations.SerializedName

data class SearchedGifsDto(
    @SerializedName("data")
    val data: List<GifInfo>,
)

data class GifInfo(
    @SerializedName("id")
    val id: String,
    @SerializedName("images")
    val images: Images,
)

data class Images(
    @SerializedName("fixed_width_small")
    val small: FixedWidthSmall,
    @SerializedName("original")
    val original: Original,
)


data class FixedWidthSmall(
    @SerializedName("url")
    val url: String,
)

data class Original(
    @SerializedName("url")
    val url: String,
)
