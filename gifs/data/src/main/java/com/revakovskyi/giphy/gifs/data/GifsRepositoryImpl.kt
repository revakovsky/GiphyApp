package com.revakovskyi.giphy.gifs.data

import android.util.Log
import com.revakovskyi.giphy.core.data.local_db.DbManager
import com.revakovskyi.giphy.core.data.network.NetworkManager
import com.revakovskyi.giphy.core.domain.gifs.models.DeletedGif
import com.revakovskyi.giphy.core.domain.gifs.models.Gif
import com.revakovskyi.giphy.core.domain.gifs.models.SearchQuery
import com.revakovskyi.giphy.core.domain.util.DataError
import com.revakovskyi.giphy.core.domain.util.EmptyDataResult
import com.revakovskyi.giphy.core.domain.util.Result
import com.revakovskyi.giphy.gifs.domain.GifsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow

/***
 * Equal to the value of the amount of data downloaded from the Internet at a time,
 *  * specified in the [ApiService.LIMIT] constant.
 *  *
 *  * @see ApiService.LIMIT
 */
private const val AMOUNT_TO_DOWNLOAD = 25
private const val DEFAULT_AMOUNT_PER_PAGE = 24

class GifsRepositoryImpl(
    private val dbManager: DbManager,
    private val networkManager: NetworkManager,
) : GifsRepository {

    private val lastQuery: StateFlow<SearchQuery?> = dbManager.lastQuery
    private val deletedGifIds: StateFlow<List<String>> = dbManager.deletedGifIds


    override fun isDbEmpty(): Flow<Boolean> = dbManager.isDbEmpty()

    override fun getGifs(searchingQuery: String, page: Int): Flow<Result<List<Gif>, DataError>> {
        return flow {

            Log.d("TAG_Max", "GifsRepositoryImpl.kt: 1")
            Log.d("TAG_Max", "")

            try {
                if (searchingQuery.isEmpty()) {

                    Log.d("TAG_Max", "GifsRepositoryImpl.kt: 2")
                    Log.d("TAG_Max", "")

                    handleEmptyQuery(page)
                } else if (searchingQuery == lastQuery.value?.query) {

                    Log.d("TAG_Max", "GifsRepositoryImpl.kt: 3")
                    Log.d("TAG_Max", "")

                    handleSameQuery(page)
                } else {

                    Log.d("TAG_Max", "GifsRepositoryImpl.kt: 4")
                    Log.d("TAG_Max", "")

                    handleNewQuery(searchingQuery)
                }
            } catch (e: Exception) {

                Log.d("TAG_Max", "GifsRepositoryImpl.kt: 5")
                Log.d("TAG_Max", "")

                emit(Result.Error(DataError.Network.UNKNOWN))
            }
        }
    }

    override fun getGifById(gifId: String): Flow<Result<Gif, DataError.Network>> {
        return flow {
            networkManager.getGifById(gifId)
        }
    }

    override fun deleteGif(gifId: String): Flow<EmptyDataResult<DataError.Local>> {
        return dbManager.insertDeletedGif(
            DeletedGif(
                gifId = gifId,
                query = lastQuery.value?.query ?: ""
            )
        )
    }

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.handleEmptyQuery(page: Int) {

        Log.d("TAG_Max", "GifsRepositoryImpl.kt: lastQuery = ${lastQuery.value?.query}")
        Log.d("TAG_Max", "")

        lastQuery.value?.let { query ->
            if (page > query.currentPage) {

                Log.d("TAG_Max", "GifsRepositoryImpl.kt: a new page for the last query")
                Log.d("TAG_Max", "GifsRepositoryImpl.kt: page = $page")
                Log.d("TAG_Max", "GifsRepositoryImpl.kt: lastPage = ${query.currentPage}")
                Log.d("TAG_Max", "")

                handleSameQuery(page)
            } else {
                dbManager.updateCurrentPage(page)
                val gifs = dbManager.loadGifsByQuery(queryId = query.id)

                Log.d("TAG_Max", "GifsRepositoryImpl.kt: the same page for the last query")
                Log.d("TAG_Max", "GifsRepositoryImpl.kt: gifs = $gifs")
                Log.d("TAG_Max", "")

                if (gifs.isNotEmpty()) {
                    emit(
                        Result.Success(
                            gifs.take(DEFAULT_AMOUNT_PER_PAGE)
                        )
                    )
                } else {

                    Log.d(
                        "TAG_Max",
                        "GifsRepositoryImpl.kt: can't read gifs from db with last query"
                    )
                    Log.d("TAG_Max", "")

                    emit(Result.Error(DataError.Local.UNKNOWN))
                }
            }
        } ?: emit(Result.Success(emptyList()))
    }

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.handleSameQuery(page: Int) {
        lastQuery.value?.let { query ->
            if (page != query.currentPage) {
                val offsetToLoad = (page - 1) * AMOUNT_TO_DOWNLOAD
                val dropGifsNumber = (page - 1) * DEFAULT_AMOUNT_PER_PAGE
                val lastElementNumber = dropGifsNumber + DEFAULT_AMOUNT_PER_PAGE

                val gifs = dbManager.loadGifsByQuery(queryId = query.id)

                if (dropGifsNumber >= gifs.size || lastElementNumber > gifs.lastIndex) {

                    Log.d(
                        "TAG_Max",
                        "GifsRepositoryImpl.kt: load gifs from remote for the page $page"
                    )
                    Log.d(
                        "TAG_Max",
                        "GifsRepositoryImpl.kt: deletedGifIds = ${deletedGifIds.value}"
                    )
                    Log.d("TAG_Max", "")

                    val result =
                        networkManager.loadGifsFromRemote(query = query, offset = offsetToLoad)
                    handleNetworkResult(
                        result = result,
                        skipGifsAmount = dropGifsNumber,
                        query = query
                    )
                } else {
                    emitPaginatedGifsFromDb(
                        gifs = gifs,
                        skipGifsAmount = dropGifsNumber
                    )
                }

                dbManager.updateCurrentPage(page)
            } else {

                Log.d("TAG_Max", "GifsRepositoryImpl.kt: the same page - don't do anything")
                Log.d("TAG_Max", "")

                emit(Result.Error(DataError.Local.THE_SAME_DATA))
            }
        }
    }

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.handleNewQuery(
        searchingQuery: String,
    ) {
        val offset = 0
        val query = SearchQuery(query = searchingQuery, currentPage = 1)

        Log.d("TAG_Max", "GifsRepositoryImpl.kt: searchingQuery = $searchingQuery")
        Log.d("TAG_Max", "")

        Log.d("TAG_Max", "GifsRepositoryImpl.kt: lastQuery = ${lastQuery.value}")
        Log.d("TAG_Max", "")

        val result = networkManager.loadGifsFromRemote(query = query, offset = offset)

        handleNetworkResult(
            result = result,
            query = query,
            skipGifsAmount = offset,
            shouldClearPreviousGifs = true,
        )
    }

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.handleNetworkResult(
        result: Result<List<Gif>, DataError.Network>,
        query: SearchQuery,
        skipGifsAmount: Int,
        shouldClearPreviousGifs: Boolean = false,
    ) {
        when (result) {
            is Result.Success -> {

                Log.d("TAG_Max", "GifsRepositoryImpl.kt: successful network result!")
                Log.d("TAG_Max", "")

                val filteredGifEntities = result.data
                    .filterNot { it.id in deletedGifIds.value }

                Log.d(
                    "TAG_Max",
                    "GifsRepositoryImpl.kt: filteredGifEntities = $filteredGifEntities"
                )
                Log.d("TAG_Max", "GifsRepositoryImpl.kt: resultDataSize = ${result.data.size}")
                Log.d(
                    "TAG_Max",
                    "GifsRepositoryImpl.kt: unique GIFs count = ${result.data.distinctBy { it.id }.size}"
                )
                Log.d("TAG_Max", "")

                saveQueryAndGifs(
                    filteredGifEntities = filteredGifEntities,
                    skipGifsAmount = skipGifsAmount,
                    query = query,
                    shouldClearPreviousGifs = shouldClearPreviousGifs
                )
            }

            is Result.Error -> {

                Log.d("TAG_Max", "GifsRepositoryImpl.kt: network error result!")
                Log.d("TAG_Max", "")

                emit(Result.Error(result.error))
            }
        }
    }

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.saveQueryAndGifs(
        filteredGifEntities: List<Gif>,
        skipGifsAmount: Int,
        query: SearchQuery,
        shouldClearPreviousGifs: Boolean,
    ) {
        val updatingResult = dbManager.saveQueryAndGifs(
            query = query,
            gifs = filteredGifEntities,
            clearPrevious = shouldClearPreviousGifs,
        )

        when (updatingResult) {
            is Result.Error -> {

                Log.d(
                    "TAG_Max",
                    "GifsRepositoryImpl.kt: problem with gif saving - ${updatingResult.error}"
                )
                Log.d("TAG_Max", "")

                emit(updatingResult)
            }

            is Result.Success -> {

                Log.d("TAG_Max", "GifsRepositoryImpl.kt: saved into db successfully")
                Log.d("TAG_Max", "")

                emitPaginatedGifsFromDb(updatingResult.data, skipGifsAmount)
            }
        }
    }

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.emitPaginatedGifsFromDb(
        gifs: List<Gif>,
        skipGifsAmount: Int,
    ) {

        Log.d(
            "TAG_Max",
            "GifsRepositoryImpl.kt: we have enough gifs in the db for current query and take them"
        )
        Log.d("TAG_Max", "")

        emit(
            Result.Success(
                gifs
                    .drop(skipGifsAmount)
                    .take(DEFAULT_AMOUNT_PER_PAGE)
            )
        )
    }

}
