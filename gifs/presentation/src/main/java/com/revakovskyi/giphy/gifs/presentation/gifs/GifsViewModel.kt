package com.revakovskyi.giphy.gifs.presentation.gifs

import androidx.lifecycle.viewModelScope
import com.revakovskyi.giphy.core.domain.gifs.Gif
import com.revakovskyi.giphy.core.domain.gifs.SearchQuery
import com.revakovskyi.giphy.core.domain.util.DataError
import com.revakovskyi.giphy.core.domain.util.Result
import com.revakovskyi.giphy.core.presentation.ui.uitls.QueryValidator
import com.revakovskyi.giphy.core.presentation.ui.uitls.UiText
import com.revakovskyi.giphy.core.presentation.ui.uitls.asUiText
import com.revakovskyi.giphy.core.presentation.view_model.BaseViewModel
import com.revakovskyi.giphy.gifs.domain.GifsRepository
import com.revakovskyi.giphy.gifs.presentation.R
import com.revakovskyi.giphy.gifs.presentation.gifs.utils.PageDirection
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class GifsViewModel(
    private val gifsRepository: GifsRepository,
    private val queryValidator: QueryValidator,
) : BaseViewModel<GifsState, GifsAction, GifsEvent>(GifsState()) {

    private val _currentInputQuery = MutableStateFlow("")
    private val currentInputQuery: StateFlow<String> = _currentInputQuery.asStateFlow()

    private val _lastSuccessfulState = MutableStateFlow<GifsState?>(null)


    init {
        handleLastQuery()
        observeQueryAndPageChanges()
    }


    override fun onAction(action: GifsAction) {
        when (action) {
            GifsAction.Search -> processNewQuery()
            GifsAction.ClearQuery -> clearSearchingQuery()
            is GifsAction.ValidateQuery -> validateAndSetQuery(action.query)
            is GifsAction.ChangePage -> changePage(action.pageDirection)
            is GifsAction.OpenOriginalGif -> openOriginalGif(action.gif)
            is GifsAction.DeleteGif -> deleteGif(action.gifId)
        }
    }

    private fun handleLastQuery() {
        viewModelScope.launch {
            val lastQuery = gifsRepository.observeLastQuery().first()

            if (lastQuery.query.isNotEmpty()) updateSearchingQuery(lastQuery)
            else loadingState(false)
        }
    }

    private fun observeQueryAndPageChanges() {
        combine(
            currentInputQuery,
            state.map { it.currentPage }.distinctUntilChanged()
        ) { query, page ->
            query to page
        }.flatMapLatest { (query, page) ->
            if (query.isNotEmpty()) gifsRepository.getGifsByQuery(query = query, page = page)
            else emptyFlow()
        }.onEach { result ->
            handleSearchRequestResult(result)
        }.launchIn(viewModelScope)
    }

    private fun processNewQuery() {
        if (currentInputQuery.value != state.value.searchingQuery) {
            loadingState(true)
            _currentInputQuery.update { "" }
            updateState { it.copy(currentPage = 1, errorMessage = null) }
            _currentInputQuery.update { state.value.searchingQuery }
        }
    }

    private fun clearSearchingQuery() {
        updateState { it.copy(searchingQuery = "", errorMessage = null) }
    }

    private fun validateAndSetQuery(query: String) {
        val validationResult = queryValidator.validate(query)
        updateState { it.copy(searchingQuery = query, errorMessage = validationResult) }
    }

    private fun changePage(pageDirection: PageDirection) {
        if (!state.value.hasError) _lastSuccessfulState.update { state.value }
        loadingState(true)

        updateState {
            it.copy(
                currentPage = when (pageDirection) {
                    PageDirection.Next -> state.value.currentPage + 1
                    PageDirection.Previous -> state.value.currentPage - 1
                    PageDirection.First -> 1
                }
            )
        }
    }

    private fun openOriginalGif(gif: Gif) {
        sendEvent(GifsEvent.OpenOriginalGif(gif))
    }

    private fun deleteGif(gifId: String) {
        viewModelScope.launch {
            loadingState(true)

            when (val deletingResult = gifsRepository.deleteGif(gifId)) {
                is Result.Error -> handleError(deletingResult)
                is Result.Success -> {
                    deleteGifFromScreen(gifId)
                    loadReplacementGif()
                }
            }
        }
    }

    private fun updateSearchingQuery(lastQuery: SearchQuery) {
        val query = lastQuery.query
        updateState { it.copy(searchingQuery = query) }
        _currentInputQuery.update { query }
    }

    private fun handleSearchRequestResult(result: Result<List<Gif>, DataError>) {
        when (result) {
            is Result.Error -> handleErrorRequestState(result)
            is Result.Success -> {
                updateState { it.copy(gifs = result.data, hasError = false) }
                _lastSuccessfulState.update { state.value }
                loadingState(false)
            }
        }
    }

    private fun handleErrorRequestState(result: Result.Error<DataError>) {
        val lastState = _lastSuccessfulState.value ?: GifsState()
        _currentInputQuery.update { lastState.searchingQuery }
        updateState {
            it.copy(
                searchingQuery = lastState.searchingQuery,
                currentPage = lastState.currentPage,
                hasError = true
            )
        }
        loadingState(false)
        handleError(result)
    }

    private fun handleError(result: Result.Error<DataError>) {
        when (result.error) {
            DataError.Local.THE_SAME_DATA -> Unit
            else -> sendEvent(GifsEvent.ShowNotification(result.error.asUiText()))
        }
    }

    private fun deleteGifFromScreen(gifId: String) {
        val updatedGifsList = state.value.gifs.filterNot { it.id == gifId }
        updateState { it.copy(gifs = updatedGifsList) }
        sendEvent(
            GifsEvent.ShowNotification(
                message = UiText.StringResource(R.string.successfully_deleted)
            )
        )
    }

    private suspend fun loadReplacementGif() {
        when (val result = gifsRepository.provideNewGif()) {
            is Result.Success -> processReceivedGif(result)
            is Result.Error -> {
                loadingState(false)
                handleError(result)
            }
        }
    }

    private fun processReceivedGif(result: Result.Success<Gif>) {
        val newGifId = result.data.id
        val existingGifIds = state.value.gifs.map { it.id }.toSet()

        if (newGifId !in existingGifIds) updateState { it.copy(gifs = state.value.gifs + result.data) }
        else handleError(Result.Error(DataError.Local.CAN_NOT_ADD_GIF))

        loadingState(false)
    }

    private fun loadingState(isLoading: Boolean) {
        updateState { it.copy(isLoading = isLoading) }
    }

}
