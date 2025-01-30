package com.revakovskyi.giphy.gifs.presentation.original_gif

import com.revakovskyi.giphy.core.presentation.ui.uitls.UiText

interface OriginalGifEvent {

    data class ShowNotification(val message: UiText) : OriginalGifEvent

}
