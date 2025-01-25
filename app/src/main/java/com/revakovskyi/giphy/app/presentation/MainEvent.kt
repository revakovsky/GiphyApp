package com.revakovskyi.giphy.app.presentation

sealed interface MainEvent {

    data object ShowInternetNotification : MainEvent

}
