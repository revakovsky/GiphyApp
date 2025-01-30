package com.revakovskyi.giphy.gifs.presentation.gifs

import com.revakovskyi.giphy.gifs.presentation.gifs.utils.PageDirection

sealed interface GifsAction {

    data object Search : GifsAction
    data object ClearQuery : GifsAction
    data class ValidateQuery(val query: String) : GifsAction
    data class ChangePage(val pageDirection: PageDirection) : GifsAction
    data class OpenOriginalGif(val gifId: String) : GifsAction
    data class DeleteGif(val gifId: String) : GifsAction

}
