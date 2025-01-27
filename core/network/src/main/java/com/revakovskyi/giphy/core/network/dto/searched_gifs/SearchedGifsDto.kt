package com.revakovskyi.giphy.core.network.dto.searched_gifs

import com.google.gson.annotations.SerializedName

data class SearchedGifsDto(
    @SerializedName("data")
    val data: List<SearchedGifsInfo>,
)

data class SearchedGifsInfo(
    @SerializedName("id")
    val id: String,
    @SerializedName("images")
    val images: Images,
)

data class Images(
    @SerializedName("fixed_width_small")
    val fixedWidthSmall: FixedWidthSmall,
)


data class FixedWidthSmall(
    @SerializedName("url")
    val url: String,
)
