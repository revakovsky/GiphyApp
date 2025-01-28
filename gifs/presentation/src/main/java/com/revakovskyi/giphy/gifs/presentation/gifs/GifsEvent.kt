package com.revakovskyi.giphy.gifs.presentation.gifs

import com.revakovskyi.giphy.core.presentation.ui.uitls.UiText

sealed interface GifsEvent {

    data class ShowNotification(val message: UiText) : GifsEvent
    data class OpenOriginalGif(val gifId: String) : GifsEvent

}
