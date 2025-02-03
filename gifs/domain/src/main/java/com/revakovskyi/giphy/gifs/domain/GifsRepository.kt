package com.revakovskyi.giphy.gifs.domain

import com.revakovskyi.giphy.core.domain.gifs.Gif
import com.revakovskyi.giphy.core.domain.gifs.SearchQuery
import com.revakovskyi.giphy.core.domain.util.DataError
import com.revakovskyi.giphy.core.domain.util.EmptyDataResult
import com.revakovskyi.giphy.core.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface GifsRepository {

    fun isDbEmpty(): Flow<Boolean>
    fun observeLastQuery(): Flow<SearchQuery>
    fun fetchGifsByQuery(query: String, page: Int): Flow<Result<List<Gif>, DataError>>
    fun getGifsByQueryId(queryId: Long): Flow<Result<List<Gif>, DataError.Local>>
    suspend fun deleteGif(gifId: String): EmptyDataResult<DataError.Local>
    suspend fun provideNewGif(): Result<Gif, DataError>

}
