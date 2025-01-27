package com.revakovskyi.giphy.app.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revakovskyi.giphy.core.data.local_db.DbManager
import com.revakovskyi.giphy.core.domain.connectivity.ConnectivityObserver
import com.revakovskyi.giphy.core.domain.connectivity.InternetStatus
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow

class MainViewModel(
    private val connectivityObserver: ConnectivityObserver,
    private val dbManager: DbManager,
) : ViewModel() {

    private val _state = MutableStateFlow(MainState())
    val state = _state.asStateFlow()

    private val _event = Channel<MainEvent>()
    val event = _event.receiveAsFlow()

    init {
        observeData()
    }

    private fun observeData() {
        val internetStatusFlow = connectivityObserver.internetStatus
        val isDataBaseEmpty = dbManager.isDbEmpty()

        combine(internetStatusFlow, isDataBaseEmpty) { internetStatus, isDbEmpty ->

            Log.d("TAG_Max", "MainViewModel.kt: internetStatus = $internetStatus")
            Log.d("TAG_Max", "MainViewModel.kt: isDbEmpty = $isDbEmpty")
            Log.d("TAG_Max", "")

            determineState(internetStatus, isDbEmpty)
        }.onEach { newState ->
            _state.value = newState
        }.launchIn(viewModelScope)
    }

    private fun determineState(
        internetStatus: InternetStatus,
        isDbEmpty: Boolean,
    ): MainState {
        return when {
            isDbEmpty && internetStatus != InternetStatus.Available -> {
                MainState(isInternetAvailable = false, canOpenGifs = false)
            }

            !isDbEmpty && internetStatus != InternetStatus.Available -> {
                _event.trySend(MainEvent.ShowInternetNotification)
                MainState(isInternetAvailable = false, canOpenGifs = true)
            }

            else -> MainState(isInternetAvailable = true, canOpenGifs = true)
        }
    }

}