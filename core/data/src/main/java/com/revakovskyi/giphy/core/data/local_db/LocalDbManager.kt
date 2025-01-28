package com.revakovskyi.giphy.core.data.local_db

import android.database.sqlite.SQLiteFullException
import android.util.Log
import com.revakovskyi.giphy.core.data.mapper.toDomain
import com.revakovskyi.giphy.core.data.mapper.toEntity
import com.revakovskyi.giphy.core.database.dao.DeletedGifsDao
import com.revakovskyi.giphy.core.database.dao.GifsDao
import com.revakovskyi.giphy.core.database.dao.SearchQueryDao
import com.revakovskyi.giphy.core.domain.gifs.models.DeletedGif
import com.revakovskyi.giphy.core.domain.gifs.models.Gif
import com.revakovskyi.giphy.core.domain.gifs.models.SearchQuery
import com.revakovskyi.giphy.core.domain.util.DataError
import com.revakovskyi.giphy.core.domain.util.EmptyDataResult
import com.revakovskyi.giphy.core.domain.util.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext

internal class LocalDbManager(
    private val gifsDao: GifsDao,
    private val searchQueryDao: SearchQueryDao,
    private val deletedGifsDao: DeletedGifsDao,
) : DbManager {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _lastQuery = MutableStateFlow<SearchQuery?>(null)
    override val lastQuery: StateFlow<SearchQuery?> = _lastQuery.asStateFlow()

    private val _deletedGifIds = MutableStateFlow<List<String>>(emptyList())
    override val deletedGifIds: StateFlow<List<String>> = _deletedGifIds.asStateFlow()


    init {
        observeForLastQuery()
        observeForDeletedGifs()
    }


    override fun isDbEmpty(): Flow<Boolean> {
        return gifsDao.isDbEmpty()
            .catch { _ -> emit(true) }
    }

    override suspend fun updateCurrentPage(page: Int) {
        searchQueryDao.updateCurrentPage(page)
    }

    override suspend fun saveNewQuery(entity: SearchQuery) {
        searchQueryDao.saveQuery(entity.toEntity())
    }

    override fun insertDeletedGif(deletedGif: DeletedGif): Flow<EmptyDataResult<DataError.Local>> {
        return flow {
            try {
                deletedGifsDao.insertDeletedGif(deletedGif.toEntity())
                emit(Result.Success(Unit))
            } catch (e: SQLiteFullException) {

                Log.d(
                    "TAG_Max",
                    "LocalDbManager.kt: SQLiteFullException 1 - ${e.printStackTrace()}"
                )
                Log.d("TAG_Max", "")

                e.printStackTrace()
                emit(Result.Error(DataError.Local.DISK_FULL))
            } catch (e: Exception) {

                Log.d("TAG_Max", "LocalDbManager.kt: Exception 1 - ${e.printStackTrace()}")
                Log.d("TAG_Max", "")

                e.printStackTrace()
                emit(Result.Error(DataError.Local.UNKNOWN))
            }
        }
    }

    override suspend fun saveQueryAndGifs(
        query: SearchQuery,
        gifs: List<Gif>,
        clearPrevious: Boolean,
    ): Result<List<Gif>, DataError> {

        Log.d("TAG_Max", "LocalDbManager.kt: saveQueryAndGifs")
        Log.d("TAG_Max", "")

        return try {
            if (clearPrevious) gifsDao.clearGifs()
            searchQueryDao.saveQuery(query.toEntity())
            gifsDao.saveGifs(gifs.map { it.toEntity(query.id) })

            Log.d("TAG_Max", "LocalDbManager.kt: updateGifsInLocalDb - result success")
            Log.d("TAG_Max", "")

            Result.Success(loadGifsByQuery(queryId = query.id))
        } catch (e: SQLiteFullException) {
            e.printStackTrace()
            Result.Error(DataError.Local.DISK_FULL)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(DataError.Local.UNKNOWN)
        }
    }

    override suspend fun loadGifsByQuery(queryId: Long): List<Gif> = withContext(Dispatchers.IO) {
        gifsDao.getGifsByQuery(queryId).map { it.toDomain() }
    }

    private fun observeForLastQuery() {
        searchQueryDao.getLastQuery()
            .onEach { query ->
                _lastQuery.value = query?.toDomain()
            }.launchIn(scope)
    }

    private fun observeForDeletedGifs() {
        deletedGifsDao.getDeletedGifIdsByQuery(lastQuery.value?.query ?: "")
            .onEach { deletedGifs ->
                _deletedGifIds.value = deletedGifs
            }.launchIn(scope)
    }

}
