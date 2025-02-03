package com.revakovskyi.giphy.core.data.local_db

import android.database.sqlite.SQLiteFullException
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

    override fun isDbEmpty(): Flow<Boolean> {
        return gifsDao.isDbEmpty()
            .catch { e ->
                e.printStackTrace()
                emit(false)
            }
    }

    override suspend fun markQueryAsSuccessful(queryId: Long) {
        searchQueryDao.markQueryAsSuccessful(queryId)
    }

    override suspend fun updateGifsMaxPosition(queryId: Long) {
        val maxPosition = getMaxGifPosition(queryId)
        searchQueryDao.updateGifsMaxPosition(queryId, maxPosition)
    }

    override suspend fun updateCurrentPage(currentPage: Int): EmptyDataResult<DataError.Local> {
        return safeDbCall {
            searchQueryDao.updateCurrentPage(lastQuery.value.id, currentPage)
        }
    }

    override suspend fun saveOrUpdateQuery(searchQuery: SearchQuery): EmptyDataResult<DataError.Local> {
        return safeDbCall {
            searchQueryDao.getSearchQueryByQueryText(searchQuery.query)?.let { existingQuery ->
                searchQueryDao.updateQuery(
                    searchQuery.toEntity().copy(
                        id = existingQuery.id,
                        wasSuccessful = existingQuery.wasSuccessful,
                        timestamp = System.currentTimeMillis(),
                        currentPage = lastQuery.value.currentPage
                    )
                )
            } ?: searchQueryDao.saveQuery(searchQuery.toEntity())

            searchQueryDao.deleteOldQueries()
        }
    }

    override suspend fun saveGifs(gifs: List<Gif>): EmptyDataResult<DataError.Local> {
        return safeDbCall {
            val queryId = _lastQuery.value.id
            val maxPosition = getMaxGifPosition(queryId)

            val gifEntitiesWithPositions = gifs.mapIndexed { index, gif ->
                gif.toEntity().copy(position = maxPosition + index + 1)
            }
            gifsDao.saveGifs(gifEntitiesWithPositions)
        }
    }

    override suspend fun getGifsByQuery(
        queryId: Long,
        limit: Int,
        pageOffset: Int,
    ): Result<List<Gif>, DataError.Local> {
        return safeDbCall {
            gifsDao.getGifsByQuery(queryId, limit, pageOffset).map { it.toDomain() }
        }
    }

    override fun getGifsByQueryId(queryId: Long): Flow<Result<List<Gif>, DataError.Local>> {
        return flow {
            try {
                gifsDao.getGifsByQueryId(queryId).collect { entities ->
                    emit(Result.Success(entities.map { it.toDomain() }))
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

    override suspend fun getSearchQueryByQueryText(queryText: String): SearchQuery? {
        return searchQueryDao.getSearchQueryByQueryText(queryText)?.toDomain()
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
            .onEach { query -> _lastQuery.update { query } }
            .catch { e -> e.printStackTrace() }
            .launchIn(scope)
    }

}
