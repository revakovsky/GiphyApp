package com.revakovskyi.giphy.core.data.local_db

import com.revakovskyi.giphy.core.domain.gifs.models.DeletedGif
import com.revakovskyi.giphy.core.domain.gifs.models.Gif
import com.revakovskyi.giphy.core.domain.gifs.models.SearchQuery
import com.revakovskyi.giphy.core.domain.util.DataError
import com.revakovskyi.giphy.core.domain.util.EmptyDataResult
import com.revakovskyi.giphy.core.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow

interface DbManager {

    val lastQuery: StateFlow<SearchQuery?>
    val deletedGifIds: StateFlow<List<String>>

    fun isDbEmpty(): Flow<Boolean>
    suspend fun updateCurrentPage(page: Int)
    suspend fun saveNewQuery(entity: SearchQuery)
    fun insertDeletedGif(deletedGif: DeletedGif): Flow<EmptyDataResult<DataError.Local>>
    suspend fun saveQueryAndGifs(
        query: SearchQuery,
        gifs: List<Gif>,
        clearPrevious: Boolean
    ): Result<List<Gif>, DataError>
    suspend fun loadGifsByQuery(queryId: Long): List<Gif>

}
