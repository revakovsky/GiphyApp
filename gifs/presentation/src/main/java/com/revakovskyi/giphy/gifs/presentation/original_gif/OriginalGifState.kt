package com.revakovskyi.giphy.gifs.presentation.original_gif

import com.revakovskyi.giphy.core.domain.gifs.Gif

data class OriginalGifState(
    val isLoading: Boolean = true,
    val gifs: List<Gif> = emptyList(),
    val currentGifId: String = "",
    val currentIndex: Int = 0,
) {

    val hasPrevious: Boolean get() = currentIndex > 0
    val hasNext: Boolean get() = currentIndex < gifs.size - 1

}
