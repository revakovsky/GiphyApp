package com.revakovskyi.giphy.core.domain.gifs.models

data class SearchQuery(
    val id: Long,
    val query: String,
    val currentPage: Int,
)
