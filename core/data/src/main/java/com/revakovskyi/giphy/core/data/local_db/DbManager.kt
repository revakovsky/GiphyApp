package com.revakovskyi.giphy.core.data.local_db

import com.revakovskyi.giphy.core.domain.gifs.Gif
import com.revakovskyi.giphy.core.domain.gifs.SearchQuery
import com.revakovskyi.giphy.core.domain.util.DataError
import com.revakovskyi.giphy.core.domain.util.EmptyDataResult
import com.revakovskyi.giphy.core.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface DbManager {

    val lastQuery: StateFlow<SearchQuery>

    fun isDbEmpty(): Flow<Boolean>
    suspend fun clearUnsuccessfulSearchQueries()
    suspend fun markQueryAsSuccessful(queryId: Long)
    suspend fun updateGifsMaxPosition(queryId: Long)
    suspend fun saveCurrentPage(currentPage: Int): EmptyDataResult<DataError.Local>
    suspend fun saveOrUpdateQuery(searchQuery: SearchQuery): EmptyDataResult<DataError.Local>
    suspend fun saveGifs(gifs: List<Gif>): EmptyDataResult<DataError.Local>
    suspend fun getGifsByQuery(
        queryId: Long,
        gifsAmount: Int,
        pageOffset: Int,
    ): Result<List<Gif>, DataError.Local>
    fun getGifsByQueryId(queryId: Long): Flow<Result<List<Gif>, DataError.Local>>
    suspend fun getSearchQueryByQueryText(queryText: String): SearchQuery?
    suspend fun deleteGif(gifId: String): EmptyDataResult<DataError.Local>
    suspend fun getMaxGifPosition(queryId: Long): Int
    suspend fun updateDeletedGifsAmount(deletedGifsAmount: Int)

}
