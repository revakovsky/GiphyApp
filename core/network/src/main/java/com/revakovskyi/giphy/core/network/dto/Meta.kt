package com.revakovskyi.giphy.core.network.dto

import com.google.gson.annotations.SerializedName

data class Meta(
    @SerializedName("status")
    val status: Int,
)
