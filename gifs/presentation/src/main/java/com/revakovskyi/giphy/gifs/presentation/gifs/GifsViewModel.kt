package com.revakovskyi.giphy.gifs.presentation.gifs

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revakovskyi.giphy.core.domain.util.Result
import com.revakovskyi.giphy.core.presentation.uitls.UiText
import com.revakovskyi.giphy.core.presentation.uitls.asUiText
import com.revakovskyi.giphy.gifs.domain.GifsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow

class GifsViewModel(
    private val gifsRepository: GifsRepository,
) : ViewModel() {

    private val _event = Channel<UiText>()
    val event = _event.receiveAsFlow()

    init {
        gifsRepository.getGifs(
            searchingQuery = "lol",
        )
            .onEach { result ->
                when (result) {
                    is Result.Error -> _event.trySend(result.error.asUiText())

                    is Result.Success -> {
                        val data = result.data

                        Log.d("TAG_Max", "GifsViewModel.kt: Gifs: $data")
                        Log.d("TAG_Max", "")

                        // TODO:
                        /***
                        - if result.data - show a text with make a request
                         */

                    }
                }
            }.launchIn(viewModelScope)
    }

}
