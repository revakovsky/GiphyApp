package com.revakovskyi.giphy.gifs.presentation.original_gif

import android.util.Log
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
import kotlinx.coroutines.launch

class OriginalGifViewModel(
    private val gifsRepository: GifsRepository,
) : ViewModel() {

    private val _originalGifState = MutableStateFlow(OriginalGifState())
    val originalGifState: StateFlow<OriginalGifState> = _originalGifState.asStateFlow()

    private val _event = Channel<OriginalGifEvent>()
    val event: Flow<OriginalGifEvent> = _event.receiveAsFlow()


    fun onAction(action: OriginalGifAction) {
        when (action) {
            is OriginalGifAction.InitializeGif -> fetchOriginalGifs(action.gif)
            OriginalGifAction.NextGif -> moveToNextGif()
            OriginalGifAction.PreviousGif -> moveToPreviousGif()
        }
    }

    private fun fetchOriginalGifs(gif: Gif) {
        viewModelScope.launch {
            gifsRepository.getGifsByQueryId(gif.queryId)
                .onEach { result ->
                    when (result) {
                        is Result.Error -> processErrorResult(gif, result)
                        is Result.Success -> processSuccessResult(gif, result)
                    }
                }.launchIn(viewModelScope)
        }
    }

    private suspend fun processErrorResult(gif: Gif, result: Result.Error<DataError>) {

        Log.d("TAG_Max", "OriginalGifViewModel.kt: processErrorResult - ${result.error.asUiText()}")
        Log.d("TAG_Max", "")

        _originalGifState.update {
            it.copy(isLoading = false, currentGifId = gif.id)
        }

        handleErrorResult(result)
    }

    private fun processSuccessResult(gif: Gif, result: Result.Success<List<Gif>>) {
        val gifs = result.data

        Log.d("TAG_Max", "GifsViewModel.kt: processSuccessResult gifs = $gifs")
        Log.d("TAG_Max", "GifsViewModel.kt: size = ${gifs.size}")
        Log.d("TAG_Max", "")

        val currentIndex = gifs.indexOfFirst { it.id == gif.id }

        _originalGifState.update {
            it.copy(
                gifs = gifs,
                currentGifId = gif.id,
                currentIndex = currentIndex,
                isLoading = false,
            )
        }
    }

    private suspend fun handleErrorResult(result: Result.Error<DataError>) {
        when (result.error) {
            DataError.Local.THE_SAME_DATA -> Unit
            else -> _event.send(OriginalGifEvent.ShowNotification(result.error.asUiText()))
        }
    }

    private fun moveToNextGif() {
        _originalGifState.value.let { state ->
            if (state.hasNext) {
                val newIndex = state.currentIndex + 1
                _originalGifState.update {
                    it.copy(
                        currentGifId = state.gifs[newIndex].id,
                        currentIndex = newIndex
                    )
                }
            }
        }
    }

    private fun moveToPreviousGif() {
        _originalGifState.value.let { state ->
            if (state.hasPrevious) {
                val newIndex = state.currentIndex - 1
                _originalGifState.update {
                    it.copy(
                        currentGifId = state.gifs[newIndex].id,
                        currentIndex = newIndex
                    )
                }
            }
        }
    }

}
