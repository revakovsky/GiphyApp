package com.revakovskyi.giphy.gifs.presentation.gifs

import com.revakovskyi.giphy.core.domain.gifs.models.Gif
import com.revakovskyi.giphy.core.presentation.ui.uitls.UiText

data class GifsState(
    val isLoading: Boolean = true,
    val gifs: List<Gif> = emptyList(),
    val currentPage: Int = 1,
    val searchingQuery: String = "",
    val errorMessage: UiText? = null,
)
