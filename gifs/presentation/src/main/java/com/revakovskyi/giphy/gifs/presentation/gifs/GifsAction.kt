package com.revakovskyi.giphy.gifs.presentation.gifs

sealed interface GifsAction {

    data object Search : GifsAction
    data object Cancel : GifsAction
    data object ClearQuery : GifsAction
    data class QueryEntered(val query: String) : GifsAction
    data class GetGifsForPage(val page: Int) : GifsAction
    data class OpenOriginalGif(val gifId: String) : GifsAction
    data class DeleteGif(val gifId: String) : GifsAction

}
