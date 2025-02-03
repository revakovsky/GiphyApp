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
import kotlinx.coroutines.flow.update
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
            clearUnsuccessfulSearchQueries()
            ensureDefaultQueryExists()
            observeForLastQuery()
        }
    }


    override suspend fun clearUnsuccessfulSearchQueries() {
        searchQueryDao.clearUnsuccessfulSearchQueries()
    }

    override suspend fun markQueryAsSuccessful(queryId: Long) {
        searchQueryDao.markQueryAsSuccessful(queryId)
    }

    override suspend fun updateGifsMaxPosition(queryId: Long) {
        val maxPosition = getMaxGifPosition(queryId)
        searchQueryDao.updateGifsMaxPosition(
            queryId = queryId,
            maxPosition = maxPosition,
        )
    }

    override fun isDbEmpty(): Flow<Boolean> {
        return gifsDao.isDbEmpty()
            .catch { e ->
                e.printStackTrace()
                emit(false)
            }
    }

    override suspend fun saveCurrentPage(currentPage: Int): EmptyDataResult<DataError.Local> {
        return safeDbCall {
            searchQueryDao.saveCurrentPage(lastQuery.value.id, currentPage)

//            Log.d("TAG_Max", "LocalDbManager.kt: saveCurrentPage")
//            Log.d("TAG_Max", "LocalDbManager.kt: currentPage = $currentPage")
//            Log.d("TAG_Max", "LocalDbManager.kt: lastQuery = ${lastQuery.value}")
//
//            val lastQuery = lastQuery.value.copy(currentPage = currentPage)
//
//            Log.d("TAG_Max", "LocalDbManager.kt: lastQueryWithNewPage = $lastQuery")
//            Log.d("TAG_Max", "")
//
//            searchQueryDao.updateQuery(lastQuery.toEntity())
        }
    }

    override suspend fun saveOrUpdateQuery(searchQuery: SearchQuery): EmptyDataResult<DataError.Local> {
        return safeDbCall {
            val existingQuery = searchQueryDao.getQueryByText(searchQuery.query)

            Log.d("TAG_Max", "LocalDbManager.kt: searchQueryToSave = $searchQuery")
            Log.d("TAG_Max", "LocalDbManager.kt: suchQueryExist = ${existingQuery != null}")
            Log.d("TAG_Max", "LocalDbManager.kt: existingQuery = $existingQuery")

            if (existingQuery == null) {

                Log.d("TAG_Max", "LocalDbManager.kt: saveQuery")

                searchQueryDao.saveQuery(searchQuery.toEntity())
            } else {

                Log.d("TAG_Max", "LocalDbManager.kt: updateQuery")
                val copy = searchQuery.toEntity().copy(
                    id = existingQuery.id,
                    wasSuccessful = existingQuery.wasSuccessful,
                    timestamp = System.currentTimeMillis(),
                    currentPage = lastQuery.value.currentPage
                )
                searchQueryDao.updateQuery(copy)
            }

            Log.d("TAG_Max", "")

            searchQueryDao.deleteOldQueries()
        }
    }

    override suspend fun saveGifs(gifs: List<Gif>): EmptyDataResult<DataError.Local> {
        return safeDbCall {
            val queryId = _lastQuery.value.id

            Log.d("TAG_Max", "LocalDbManager.kt: saveGifsIntoDb")
            Log.d("TAG_Max", "LocalDbManager.kt: lastQuery = $queryId")
            Log.d("TAG_Max", "")

//            gifsDao.saveGifs(gifs.map { it.toEntity() })

            val maxPosition = getMaxGifPosition(queryId)

            Log.d("TAG_Max", "LocalDbManager.kt: currentMaxPosition = $maxPosition")

            // 2. Обновляем `position` у новых GIF'ок
            val updatedGifs = gifs.mapIndexed { index, gif ->
                gif.toEntity().copy(position = maxPosition + index + 1)
            }

            gifsDao.saveGifs(updatedGifs)
        }
    }

    override suspend fun getGifsByQuery(
        queryId: Long,
        gifsAmount: Int,
        pageOffset: Int,
    ): Result<List<Gif>, DataError.Local> {
        return try {
            val gifs = gifsDao.getGifsByQuery(queryId, gifsAmount, pageOffset).map { it.toDomain() }

            Log.d("TAG_Max", "LocalDbManager.kt: observeGifsFromDbByQuery")
            Log.d("TAG_Max", "LocalDbManager.kt: queryId = $queryId")
            Log.d("TAG_Max", "LocalDbManager.kt: gifsAmount = $gifsAmount")
            Log.d("TAG_Max", "LocalDbManager.kt: pageOffset = $pageOffset")
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

    override suspend fun getSearchQueryByQueryText(queryText: String): SearchQuery? {
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

    override suspend fun deleteGif(gifId: String): EmptyDataResult<DataError.Local> {
        return safeDbCall {
            gifsDao.deleteGif(gifId)
        }
    }

    override suspend fun getMaxGifPosition(queryId: Long): Int {
        return gifsDao.getMaxGifPosition(queryId) ?: 0
    }

    override suspend fun updateDeletedGifsAmount(deletedGifsAmount: Int) {
        searchQueryDao.updateDeletedGifsAmount(
            queryId = lastQuery.value.id,
            deletedGifsAmount = deletedGifsAmount
        )
    }

    private suspend fun ensureDefaultQueryExists() {
        searchQueryDao.getLastQuery()
            .firstOrNull()
            ?: saveOrUpdateQuery(defaultQuery)
    }

    private fun observeForLastQuery() {
        searchQueryDao.getLastQuery()
            .map { it?.toDomain() ?: defaultQuery }
            .onEach { query ->

                Log.d("TAG_Max", "LocalDbManager.kt: updatedQuery = $query")
                Log.d("TAG_Max", "")

                _lastQuery.update { query }
            }
            .catch { e -> e.printStackTrace() }
            .launchIn(scope)
    }

}
