package com.revakovskyi.giphy.app.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _state = MutableStateFlow(MainState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            // TODO: improve it later

            delay(2000)
            _state.update {
                it.copy(
                    isInternetAvailable = false,
                    canOpenGifs = false,
                )
            }
        }
    }

}