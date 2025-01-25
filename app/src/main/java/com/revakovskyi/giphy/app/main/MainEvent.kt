package com.revakovskyi.giphy.app.main

sealed interface MainEvent {

    data object ShowInternetNotification : MainEvent

}
