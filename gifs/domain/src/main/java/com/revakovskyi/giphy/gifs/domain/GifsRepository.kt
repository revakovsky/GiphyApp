package com.revakovskyi.giphy.gifs.domain

import com.revakovskyi.giphy.core.domain.gifs.models.Gif
import com.revakovskyi.giphy.core.domain.util.DataError
import com.revakovskyi.giphy.core.domain.util.EmptyDataResult
import com.revakovskyi.giphy.core.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface GifsRepository {

    fun isDbEmpty(): Flow<Boolean>
    fun getGifs(searchingQuery: String = "", page: Int = 1): Flow<Result<List<Gif>, DataError>>
    fun getGifById(gifId: String): Flow<Result<Gif, DataError.Network>>
    fun deleteGif(gifId: String): Flow<EmptyDataResult<DataError.Local>>

}
