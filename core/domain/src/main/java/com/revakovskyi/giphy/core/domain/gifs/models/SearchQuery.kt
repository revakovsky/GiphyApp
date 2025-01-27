package com.revakovskyi.giphy.core.domain.gifs.models

data class SearchQuery(
    val id: Long = 1,
    val query: String,
    val currentPage: Int,
)
