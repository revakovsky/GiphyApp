package com.revakovskyi.giphy.core.data.local_db

import android.database.sqlite.SQLiteFullException
import android.util.Log
import com.revakovskyi.giphy.core.data.mapper.toDomain
import com.revakovskyi.giphy.core.data.mapper.toEntity
import com.revakovskyi.giphy.core.data.utils.safeDbCall
import com.revakovskyi.giphy.core.database.dao.GifsDao
import com.revakovskyi.giphy.core.database.dao.SearchQueryDao
import com.revakovskyi.giphy.core.domain.gifs.Gif
import com.revakovskyi.giphy.core.domain.gifs.SearchQuery
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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

internal class LocalDbManager(
    private val gifsDao: GifsDao,
    private val searchQueryDao: SearchQueryDao,
) : DbManager {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val defaultQuery = SearchQuery(query = "", currentPage = 1)

    private val _lastQuery = MutableStateFlow(defaultQuery)
    override val lastQuery: StateFlow<SearchQuery> = _lastQuery.asStateFlow()


    init {
        scope.launch {
            ensureDefaultQueryExists()
            observeForLastQuery()
        }
    }


    override fun isDbEmpty(): Flow<Boolean> {
        return gifsDao.isDbEmpty()
            .catch { e ->
                e.printStackTrace()
                emit(false)
            }
    }

    override suspend fun saveCurrentPage(
        queryId: Long,
        currentPage: Int,
    ): EmptyDataResult<DataError.Local> {
        return safeDbCall {
            searchQueryDao.saveCurrentPage(queryId, currentPage)
        }
    }

    override suspend fun saveNewQuery(entity: SearchQuery): EmptyDataResult<DataError.Local> {
        return safeDbCall {
            searchQueryDao.saveQuery(entity.toEntity())
            searchQueryDao.deleteOldQueries()
        }
    }

    override suspend fun saveGifs(gifs: List<Gif>): EmptyDataResult<DataError.Local> {
        return safeDbCall {
            val queryId = _lastQuery.value.id

            Log.d("TAG_Max", "LocalDbManager.kt: saveGifsIntoDb")
            Log.d("TAG_Max", "LocalDbManager.kt: lastQuery = $queryId")
            Log.d("TAG_Max", "")

            gifsDao.saveGifs(gifs.map { it.toEntity() })
        }
    }

    override suspend fun observeGifsFromDbByQuery(
        queryId: Long,
        gifsAmount: Int,
        pageOffset: Int,
    ): Result<List<Gif>, DataError.Local> {
        return try {
            val gifs = gifsDao.getGifsByQuery(queryId, gifsAmount, pageOffset).map { it.toDomain() }

            Log.d("TAG_Max", "LocalDbManager.kt: observeGifsFromDbByQuery")
            Log.d("TAG_Max", "LocalDbManager.kt: gifs = $gifs")
            Log.d("TAG_Max", "")

            Result.Success(gifs)
        } catch (e: Exception) {
            Log.d("TAG_Max", "LocalDbManager.kt: observeGifsFromDbByQuery ERROR")
            Log.d("TAG_Max", "LocalDbManager.kt: error = ${e.localizedMessage}")
            Log.d("TAG_Max", "")

            e.printStackTrace()

            if (e is CancellationException) throw e
            Result.Error(
                if (e is SQLiteFullException) DataError.Local.DISK_FULL
                else DataError.Local.UNKNOWN
            )
        }
    }

    override suspend fun getQueryByText(queryText: String): SearchQuery? {
        return searchQueryDao.getQueryByText(queryText)?.toDomain()
    }

    override fun getGifsByQueryId(queryId: Long): Flow<Result<List<Gif>, DataError.Local>> {
        return flow {
            try {
                gifsDao.getGifsByQueryId(queryId).collect { entities ->
                    val gifs = entities.map { it.toDomain() }
                    emit(Result.Success(gifs))
                }
            } catch (e: SQLiteFullException) {
                e.printStackTrace()
                emit(Result.Error(DataError.Local.DISK_FULL))
            } catch (e: Exception) {
                e.printStackTrace()
                if (e is CancellationException) throw e
                emit(Result.Error(DataError.Local.UNKNOWN))
            }
        }
    }

    private suspend fun ensureDefaultQueryExists() {
        searchQueryDao.getLastQuery()
            .firstOrNull()
            ?: saveNewQuery(defaultQuery)
    }

    private fun observeForLastQuery() {
        searchQueryDao.getLastQuery()
            .map { it?.toDomain() ?: defaultQuery }
            .onEach { query ->

                Log.d("TAG_Max", "LocalDbManager.kt: updatedQuery = $query")
                Log.d("TAG_Max", "")

                _lastQuery.value = query
            }
            .catch { e -> e.printStackTrace() }
            .launchIn(scope)
    }

}
