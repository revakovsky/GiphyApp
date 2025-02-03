package com.revakovskyi.giphy.core.domain.gifs

data class SearchQuery(
    val id: Long = 0,
    val query: String,
    val currentPage: Int,
    val deletedGifsAmount: Int = 0,
    val maxGifPositionInTable: Int = 1,
)
