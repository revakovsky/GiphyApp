package com.revakovskyi.giphy.core.network.dto.gif_by_id

import com.google.gson.annotations.SerializedName
import com.revakovskyi.giphy.core.network.dto.Meta

data class IdGif(
    @SerializedName("data")
    val data: Data,
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
    @SerializedName("original")
    val original: Original,
)

data class Original(
    @SerializedName("url")
    val url: String,
)
