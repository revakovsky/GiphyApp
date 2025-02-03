package com.revakovskyi.giphy.gifs.presentation.original_gif

import com.revakovskyi.giphy.core.domain.gifs.Gif

data class OriginalGifState(
    val isLoading: Boolean = true,
    val gifs: List<Gif> = emptyList(),
    val currentIndex: Int = 0,
)
