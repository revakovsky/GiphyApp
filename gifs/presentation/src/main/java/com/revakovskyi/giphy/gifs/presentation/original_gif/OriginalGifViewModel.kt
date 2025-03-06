package com.revakovskyi.giphy.gifs.presentation.original_gif

import androidx.lifecycle.viewModelScope
import com.revakovskyi.giphy.core.domain.gifs.Gif
import com.revakovskyi.giphy.core.domain.util.DataError
import com.revakovskyi.giphy.core.domain.util.Result
import com.revakovskyi.giphy.core.presentation.ui.uitls.asUiText
import com.revakovskyi.giphy.core.presentation.view_model.BaseViewModel
import com.revakovskyi.giphy.gifs.domain.GifsRepository
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class OriginalGifViewModel(
    private val gifsRepository: GifsRepository,
) : BaseViewModel<OriginalGifState, OriginalGifAction, OriginalGifEvent>(OriginalGifState()) {


    override fun onAction(action: OriginalGifAction) {
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
        updateState { it.copy(currentIndex = index) }
    }

    private fun processErrorResult(result: Result.Error<DataError>) {
        updateState { it.copy(isLoading = false) }
        handleErrorResult(result)
    }

    private fun processSuccessResult(gif: Gif, result: Result.Success<List<Gif>>) {
        val gifs = result.data
        val currentIndex = gifs.indexOfFirst { it.id == gif.id }

        updateState { it.copy(gifs = gifs, currentIndex = currentIndex, isLoading = false) }
    }

    private fun handleErrorResult(result: Result.Error<DataError>) {
        when (result.error) {
            DataError.Local.THE_SAME_DATA -> Unit
            else -> sendEvent(OriginalGifEvent.ShowNotification(result.error.asUiText()))
        }
    }

}
