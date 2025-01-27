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

internal class LocalDbManager(
    private val gifsDao: GifsDao,
    private val searchQueryDao: SearchQueryDao,
    private val deletedGifsDao: DeletedGifsDao,
) : DbManager {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _lastQuery = MutableStateFlow<SearchQuery?>(null)
    override val lastQuery: StateFlow<SearchQuery?> = _lastQuery.asStateFlow()

    private val _deletedGifIds = MutableStateFlow<List<String>>(emptyList())
    override val deletedGifIds: StateFlow<List<String>> = _deletedGifIds.asStateFlow()

    private val _gifs = MutableStateFlow<List<Gif>>(emptyList())
    override val gifs: StateFlow<List<Gif>> = _gifs.asStateFlow()


    init {
        observeForLastQuery()
        observeForDeletedGifs()
        observeForNewGifs()
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

    override suspend fun updateGifsInLocalDb(filteredGifs: List<Gif>): EmptyDataResult<DataError.Local> {

        Log.d("TAG_Max", "LocalDbManager.kt: updateGifsInLocalDb")
        Log.d("TAG_Max", "")

        return try {
            gifsDao.saveGifs(
                filteredGifs.map {
                    it.toEntity(lastQuery.value?.id ?: 1)
                }
            )

            Log.d("TAG_Max", "LocalDbManager.kt: updateGifsInLocalDb - result success")
            Log.d("TAG_Max", "")

            Result.Success(Unit)
        } catch (e: SQLiteFullException) {

            Log.d(
                "TAG_Max",
                "LocalDbManager.kt: updateGifsInLocalDb - result error SQLiteFullException"
            )
            Log.d("TAG_Max", "")

            e.printStackTrace()
            Result.Error(DataError.Local.DISK_FULL)
        } catch (e: Exception) {

            Log.d("TAG_Max", "LocalDbManager.kt: updateGifsInLocalDb - result error UNKNOWN")
            Log.d("TAG_Max", "LocalDbManager.kt: ${e.localizedMessage}")
            Log.d("TAG_Max", "")

            e.printStackTrace()
            Result.Error(DataError.Local.UNKNOWN)
        }
    }

    override fun insertDeletedGif(deletedGif: DeletedGif): Flow<EmptyDataResult<DataError.Local>> {
        return flow {
            try {
                deletedGifsDao.insertDeletedGif(deletedGif.toEntity())
                emit(Result.Success(Unit))
            } catch (e: SQLiteFullException) {
                e.printStackTrace()
                emit(Result.Error(DataError.Local.DISK_FULL))
            } catch (e: Exception) {
                e.printStackTrace()
                emit(Result.Error(DataError.Local.UNKNOWN))
            }
        }
    }

    override suspend fun clearPreviousGifs(): Flow<EmptyDataResult<DataError.Local>> {
        return flow {
            try {
                gifsDao.clearGifs()
                emit(Result.Success(Unit))
            } catch (e: SQLiteFullException) {
                e.printStackTrace()
                emit(Result.Error(DataError.Local.DISK_FULL))
            } catch (e: Exception) {
                e.printStackTrace()
                emit(Result.Error(DataError.Local.UNKNOWN))
            }
        }
    }

    private fun observeForLastQuery() {
        searchQueryDao.getLastQuery()
            .onEach { query ->

                Log.d("TAG_Max", "LocalDbManager.kt: observeLastQuery = $query")
                Log.d("TAG_Max", "")

                _lastQuery.value = query?.toDomain()
            }.launchIn(scope)
    }

    private fun observeForDeletedGifs() {
        deletedGifsDao.getDeletedGifIdsByQuery(lastQuery.value?.query ?: "")
            .onEach { deletedGifs ->

                Log.d("TAG_Max", "LocalDbManager.kt: deletedGifs = $deletedGifs")
                Log.d("TAG_Max", "")

                _deletedGifIds.value = deletedGifs
            }.launchIn(scope)
    }

    private fun observeForNewGifs() {
        gifsDao.getGifsByQuery(lastQuery.value?.id ?: 1)
            .onEach { gifEntities ->

                Log.d("TAG_Max", "LocalDbManager.kt: gifEntities = $gifEntities")
                Log.d("TAG_Max", "")

                _gifs.value = gifEntities.map { it.toDomain() }
            }.launchIn(scope)
    }

}
