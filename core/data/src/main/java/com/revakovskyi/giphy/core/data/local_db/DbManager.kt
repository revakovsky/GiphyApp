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
    suspend fun saveCurrentPage(queryId: Long, currentPage: Int): EmptyDataResult<DataError.Local>
    suspend fun saveNewQuery(entity: SearchQuery): EmptyDataResult<DataError.Local>
    suspend fun saveGifs(gifs: List<Gif>): EmptyDataResult<DataError.Local>
    suspend fun observeGifsFromDbByQuery(
        queryId: Long,
        gifsAmount: Int,
        pageOffset: Int,
    ): Result<List<Gif>, DataError.Local>

    suspend fun getQueryByText(queryText: String): SearchQuery?
    fun getGifsByQueryId(queryId: Long): Flow<Result<List<Gif>, DataError.Local>>

}
