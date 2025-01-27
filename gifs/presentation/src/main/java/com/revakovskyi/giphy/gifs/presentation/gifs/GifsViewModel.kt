package com.revakovskyi.giphy.gifs.presentation.gifs

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revakovskyi.giphy.core.domain.util.Result
import com.revakovskyi.giphy.core.presentation.ui.uitls.QueryValidator
import com.revakovskyi.giphy.core.presentation.ui.uitls.UiText
import com.revakovskyi.giphy.core.presentation.ui.uitls.asUiText
import com.revakovskyi.giphy.gifs.domain.GifsRepository
import com.revakovskyi.giphy.gifs.presentation.R
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

class GifsViewModel(
    private val gifsRepository: GifsRepository,
    private val queryValidator: QueryValidator,
) : ViewModel() {

    private val _state = MutableStateFlow(GifsState())
    val state = _state.asStateFlow()

    private val _event = Channel<GifsEvent>()
    val event = _event.receiveAsFlow()


    init {
        observeForGifs()
        observeForSearchingQuery()
    }


    fun onAction(action: GifsAction) {
        when (action) {
            GifsAction.Search -> getGifsBySearchingQuery()
            GifsAction.Cancel -> hideSearchingButtons()
            GifsAction.ClearQuery -> clearSearchingQuery()
            is GifsAction.GetGifsForPage -> getGifsForPage(action.page)
            is GifsAction.OpenOriginalGif -> openOriginalGif(action.gifId)
            is GifsAction.DeleteGif -> deleteGif(action.gifId)
        }
    }

    private fun observeForGifs() {
        gifsRepository.getGifs()
            .onEach { result ->
                when (result) {
                    is Result.Error -> {
                        _state.update { it.copy(isLoading = false) }
                        _event.send(GifsEvent.ShowNotification(result.error.asUiText()))
                    }

                    is Result.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                gifs = result.data
                            )
                        }
                    }
                }
            }.launchIn(viewModelScope)
    }

    private fun observeForSearchingQuery() {
        snapshotFlow { state.value.searchingQuery }
            .onEach { input ->
                val validationResult = queryValidator.appropriateLanguage(input)
                handleLanguageValidationResult(input, validationResult)
            }.launchIn(viewModelScope)
    }

    private fun handleLanguageValidationResult(input: String, validationResult: UiText?) {
        if (validationResult == null) {
            _state.update {
                it.copy(
                    searchingQuery = input.trim(),
                    errorMessage = null,
                )
            }
        } else _state.update { it.copy(errorMessage = validationResult) }
    }

    private fun getGifsBySearchingQuery() {
        val validationResult = queryValidator.validate(state.value.searchingQuery)

        if (validationResult == null) {
            _state.update { it.copy(isLoading = true) }
            gifsRepository.getGifs(searchingQuery = state.value.searchingQuery)
        } else _state.update { it.copy(errorMessage = validationResult) }
    }

    private fun hideSearchingButtons() {
        _event.trySend(GifsEvent.HideSearchingButtons)
    }

    private fun clearSearchingQuery() {
        _state.update {
            it.copy(searchingQuery = "", errorMessage = null)
        }
    }

    private fun getGifsForPage(page: Int) {
        _state.update { it.copy(isLoading = true, currentPage = page) }

        gifsRepository.getGifs(
            searchingQuery = state.value.searchingQuery,
            page = state.value.currentPage
        )
    }

    private fun openOriginalGif(gifId: String) {
        _event.trySend(GifsEvent.OpenOriginalGif(gifId))
    }

    private fun deleteGif(gifId: String) {
        gifsRepository.deleteGif(gifId)
            .onEach { result ->
                when (result) {
                    is Result.Error -> _event.send(GifsEvent.ShowNotification(result.error.asUiText()))

                    is Result.Success -> {
                        _event.send(
                            GifsEvent.ShowNotification(
                                UiText.StringResource(R.string.successfully_deleted)
                            )
                        )
                    }
                }
            }.launchIn(viewModelScope)
    }

}
