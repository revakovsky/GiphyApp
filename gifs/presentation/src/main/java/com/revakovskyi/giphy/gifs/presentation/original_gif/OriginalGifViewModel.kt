package com.revakovskyi.giphy.gifs.presentation.original_gif

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revakovskyi.giphy.core.domain.gifs.Gif
import com.revakovskyi.giphy.core.domain.util.DataError
import com.revakovskyi.giphy.core.domain.util.Result
import com.revakovskyi.giphy.core.presentation.ui.uitls.asUiText
import com.revakovskyi.giphy.gifs.domain.GifsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

class OriginalGifViewModel(
    private val gifsRepository: GifsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(OriginalGifState())
    val state: StateFlow<OriginalGifState> = _state.asStateFlow()

    private val _event = Channel<OriginalGifEvent>()
    val event: Flow<OriginalGifEvent> = _event.receiveAsFlow()


    fun onAction(action: OriginalGifAction) {
        when (action) {
            is OriginalGifAction.InitializeGif -> getOriginalGifs(action.gif)
            is OriginalGifAction.UpdateCurrentIndex -> updateCurrentIndex(action.index)
        }
    }

    private fun getOriginalGifs(gif: Gif) {
        gifsRepository.getOriginalGifsByQueryId(gif.queryId)
            .onEach { result ->
                when (result) {
                    is Result.Error -> processErrorResult(result)
                    is Result.Success -> processSuccessResult(gif, result)
                }
            }.launchIn(viewModelScope)
    }

    private fun updateCurrentIndex(index: Int) {
        _state.update { it.copy(currentIndex = index) }
    }

    private suspend fun processErrorResult(result: Result.Error<DataError>) {
        _state.update { it.copy(isLoading = false) }
        handleErrorResult(result)
    }

    private fun processSuccessResult(gif: Gif, result: Result.Success<List<Gif>>) {
        val gifs = result.data
        val currentIndex = gifs.indexOfFirst { it.id == gif.id }

        _state.update { it.copy(gifs = gifs, currentIndex = currentIndex, isLoading = false) }
    }

    private suspend fun handleErrorResult(result: Result.Error<DataError>) {
        when (result.error) {
            DataError.Local.THE_SAME_DATA -> Unit
            else -> _event.send(OriginalGifEvent.ShowNotification(result.error.asUiText()))
        }
    }

}
