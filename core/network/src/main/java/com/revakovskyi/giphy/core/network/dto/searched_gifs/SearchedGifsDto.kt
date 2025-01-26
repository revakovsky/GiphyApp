package com.revakovskyi.giphy.core.network.dto.searched_gifs

import com.google.gson.annotations.SerializedName
import com.revakovskyi.giphy.core.network.dto.Meta

data class SearchedGifsDto(
    @SerializedName("data")
    val data: List<Data>,
    @SerializedName("meta")
    val meta: Meta,
)

data class Data(
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
