package com.revakovskyi.giphy.core.data.local_db

import com.revakovskyi.giphy.core.domain.gifs.models.DeletedGif
import com.revakovskyi.giphy.core.domain.gifs.models.Gif
import com.revakovskyi.giphy.core.domain.gifs.models.SearchQuery
import com.revakovskyi.giphy.core.domain.util.DataError
import com.revakovskyi.giphy.core.domain.util.EmptyDataResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface DbManager {

    val lastQuery: StateFlow<SearchQuery?>
    val deletedGifIds: StateFlow<List<String>>
    val gifs: StateFlow<List<Gif>>

    fun isDbEmpty(): Flow<Boolean>
    suspend fun updateCurrentPage(page: Int)
    suspend fun saveNewQuery(entity: SearchQuery)
    suspend fun updateGifsInLocalDb(filteredGifs: List<Gif>): EmptyDataResult<DataError.Local>
    fun insertDeletedGif(deletedGif: DeletedGif): Flow<EmptyDataResult<DataError.Local>>
    suspend fun clearPreviousGifs(): Flow<EmptyDataResult<DataError.Local>>

}
