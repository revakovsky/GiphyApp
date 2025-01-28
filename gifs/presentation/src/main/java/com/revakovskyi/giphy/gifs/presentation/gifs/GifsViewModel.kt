package com.revakovskyi.giphy.gifs.presentation.gifs

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revakovskyi.giphy.core.domain.util.DataError
import com.revakovskyi.giphy.core.domain.util.Result
import com.revakovskyi.giphy.core.presentation.ui.uitls.QueryValidator
import com.revakovskyi.giphy.core.presentation.ui.uitls.UiText
import com.revakovskyi.giphy.core.presentation.ui.uitls.asUiText
import com.revakovskyi.giphy.gifs.domain.GifsRepository
import com.revakovskyi.giphy.gifs.presentation.R
import com.revakovskyi.giphy.gifs.presentation.gifs.utils.PageDirection
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

class GifsViewModel(
    private val gifsRepository: GifsRepository,
    private val queryValidator: QueryValidator,
) : ViewModel() {

    private val _state = MutableStateFlow(GifsState())
    val state = _state.asStateFlow()

    private val _currentInputQuery = MutableStateFlow("")
    private val currentInputQuery = _currentInputQuery.asStateFlow()

    private val _event = Channel<GifsEvent>()
    val event = _event.receiveAsFlow()


    init {
        observeForGifs()
    }


    fun onAction(action: GifsAction) {
        when (action) {
            GifsAction.Search -> getGifsBySearchingQuery()
            GifsAction.ClearQuery -> clearSearchingQuery()
            is GifsAction.QueryEntered -> queryEntered(action.query)
            is GifsAction.GetGifsForPage -> getGifsForPage(action.pageDirection)
            is GifsAction.OpenOriginalGif -> openOriginalGif(action.gifId)
            is GifsAction.DeleteGif -> deleteGif(action.gifId)
        }
    }

    private fun observeForGifs() {
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
            gifsRepository.getGifs(searchingQuery = query, page = page)
        }.onEach { result ->
            when (result) {
                is Result.Error -> {
                    _state.update { it.copy(isLoading = false) }

                    when (result.error) {
                        DataError.Local.THE_SAME_DATA -> Unit
                        else -> _event.send(GifsEvent.ShowNotification(result.error.asUiText()))
                    }
                }

                is Result.Success -> {

                    Log.d("TAG_Max", "GifsViewModel.kt: gifs = ${result.data}")
                    Log.d("TAG_Max", "")

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

    private fun getGifsBySearchingQuery() {
        if (_currentInputQuery.value != state.value.searchingQuery) {
            _state.update {
                it.copy(
                    isLoading = true,
                    currentPage = 1,
                    errorMessage = null
                )
            }
            _currentInputQuery.update { state.value.searchingQuery }
        }
    }

    private fun clearSearchingQuery() {
        _state.update {
            it.copy(searchingQuery = "", errorMessage = null)
        }
    }

    private fun queryEntered(query: String) {
        val validationResult = queryValidator.validate(query)

        _state.update {
            it.copy(
                searchingQuery = query,
                errorMessage = validationResult
            )
        }
    }

    private fun getGifsForPage(pageDirection: PageDirection) {
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
