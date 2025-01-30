package com.revakovskyi.giphy.gifs.presentation.gifs

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revakovskyi.giphy.core.domain.gifs.Gif
import com.revakovskyi.giphy.core.domain.gifs.SearchQuery
import com.revakovskyi.giphy.core.domain.util.DataError
import com.revakovskyi.giphy.core.domain.util.Result
import com.revakovskyi.giphy.core.presentation.ui.uitls.QueryValidator
import com.revakovskyi.giphy.core.presentation.ui.uitls.asUiText
import com.revakovskyi.giphy.gifs.domain.GifsRepository
import com.revakovskyi.giphy.gifs.presentation.gifs.utils.PageDirection
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
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
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class GifsViewModel(
    private val gifsRepository: GifsRepository,
    private val queryValidator: QueryValidator,
) : ViewModel() {

    private val _state = MutableStateFlow(GifsState())
    val state: StateFlow<GifsState> = _state.asStateFlow()

    private val _currentInputQuery = MutableStateFlow("")
    private val currentInputQuery: StateFlow<String> = _currentInputQuery.asStateFlow()

    private val _event = Channel<GifsEvent>()
    val event: Flow<GifsEvent> = _event.receiveAsFlow()


    init {
        viewModelScope.launch { handleLastQuery() }
        observeQueryAndPageChanges()
    }


    fun onAction(action: GifsAction) {
        when (action) {
            GifsAction.Search -> processNewQuery()
            GifsAction.ClearQuery -> clearSearchingQuery()
            is GifsAction.ValidateQuery -> validateAndSetQuery(action.query)
            is GifsAction.ChangePage -> changePage(action.pageDirection)
            is GifsAction.OpenOriginalGif -> openOriginalGif(action.gif)
            is GifsAction.DeleteGif -> deleteGif(action.gifId)
        }
    }

    private suspend fun handleLastQuery() {
        val lastQuery = gifsRepository.observeLastQuery().first()

        Log.d("TAG_Max", "GifsViewModel.kt: gotten lastQuery = $lastQuery")
        Log.d("TAG_Max", "")

        if (lastQuery.query.isNotEmpty()) updateSearchingQuery(lastQuery)
        else _state.update { it.copy(isLoading = false) }
    }

    private fun updateSearchingQuery(lastQuery: SearchQuery) {
        val text = lastQuery.query
        _state.update { it.copy(searchingQuery = text) }
        _currentInputQuery.update { text }
    }


    private fun observeQueryAndPageChanges() {
        combine(
            currentInputQuery,
            state.map { it.currentPage }.distinctUntilChanged()
        ) { query, page ->

            Log.d("TAG_Max", "GifsViewModel.kt: update query or page")
            Log.d("TAG_Max", "GifsViewModel.kt: query = $query")
            Log.d("TAG_Max", "GifsViewModel.kt: page = $page")
            Log.d("TAG_Max", "")

            query to page
        }.flatMapLatest { (query, page) ->
            if (query.isNotEmpty()) {
                gifsRepository.fetchGifsByRequest(searchingQuery = query, page = page)
            } else emptyFlow()
        }.onEach { result ->
            handleRequestResult(result)
        }.launchIn(viewModelScope)
    }

    private suspend fun handleRequestResult(result: Result<List<Gif>, DataError>) {
        when (result) {
            is Result.Error -> {
                _state.update {
                    it.copy(isLoading = false, searchingQuery = "", errorMessage = null)
                }
                _currentInputQuery.update { state.value.searchingQuery }
                handleErrorResult(result)
            }

            is Result.Success -> {

                Log.d("TAG_Max", "GifsViewModel.kt: gifs = ${result.data}")
                Log.d("TAG_Max", "")

                _state.update {
                    it.copy(isLoading = false, gifs = result.data)
                }
            }
        }
    }

    private suspend fun handleErrorResult(result: Result.Error<DataError>) {
        when (result.error) {
            DataError.Local.THE_SAME_DATA -> Unit
            else -> _event.send(GifsEvent.ShowNotification(result.error.asUiText()))
        }
    }

    private fun processNewQuery() {
        if (_currentInputQuery.value != state.value.searchingQuery) {
            _state.update {
                it.copy(isLoading = true, currentPage = 1, errorMessage = null)
            }
            _currentInputQuery.update { state.value.searchingQuery }
        }
    }

    private fun clearSearchingQuery() {
        _state.update {
            it.copy(searchingQuery = "", errorMessage = null)
        }
    }

    private fun validateAndSetQuery(query: String) {
        val validationResult = queryValidator.validate(query)

        _state.update {
            it.copy(
                searchingQuery = query,
                errorMessage = validationResult
            )
        }
    }

    private fun changePage(pageDirection: PageDirection) {
        _state.update {
            it.copy(
                isLoading = true,
                currentPage = when (pageDirection) {
                    PageDirection.Next -> state.value.currentPage + 1
                    PageDirection.Previous -> state.value.currentPage - 1
                    PageDirection.First -> 1
                }
            )
        }
    }

    private fun openOriginalGif(gif: Gif) {
        _event.trySend(GifsEvent.OpenOriginalGif(gif))
    }

    private fun deleteGif(gifId: String) {
//        gifsRepository.deleteGif(gifId)
//            .onEach { result ->
//                when (result) {
//                    is Result.Error -> _event.send(GifsEvent.ShowNotification(result.error.asUiText()))
//
//                    is Result.Success -> {
//                        _event.send(
//                            GifsEvent.ShowNotification(
//                                UiText.StringResource(R.string.successfully_deleted)
//                            )
//                        )
//                    }
//                }
//            }.launchIn(viewModelScope)
    }

}
