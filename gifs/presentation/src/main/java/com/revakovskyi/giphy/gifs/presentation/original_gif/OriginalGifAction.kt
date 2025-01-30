package com.revakovskyi.giphy.gifs.presentation.original_gif

import com.revakovskyi.giphy.core.domain.gifs.Gif

sealed interface OriginalGifAction {

    data class InitializeGif(val gif: Gif) : OriginalGifAction
    data object NextGif : OriginalGifAction
    data object PreviousGif : OriginalGifAction

}
