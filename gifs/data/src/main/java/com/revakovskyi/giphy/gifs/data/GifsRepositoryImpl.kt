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

private const val DEFAULT_AMOUNT_PER_PAGE = 24

class GifsRepositoryImpl(
    private val dbManager: DbManager,
    private val networkManager: NetworkManager,
) : GifsRepository {

    private val lastQuery: StateFlow<SearchQuery?> = dbManager.lastQuery
    private val deletedGifIds: StateFlow<List<String>> = dbManager.deletedGifIds
    private val gifs: StateFlow<List<Gif>> = dbManager.gifs


    override fun isDbEmpty(): Flow<Boolean> = dbManager.isDbEmpty()

    override fun getGifs(searchingQuery: String, page: Int): Flow<Result<List<Gif>, DataError>> {
        return flow {

            Log.d("TAG_Max", "GifsRepositoryImpl.kt: 1")
            Log.d("TAG_Max", "")

            try {
                if (searchingQuery.isEmpty()) {

                    Log.d("TAG_Max", "GifsRepositoryImpl.kt: 2")
                    Log.d("TAG_Max", "")

                    handleEmptyQuery()
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

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.handleEmptyQuery() {

        Log.d("TAG_Max", "GifsRepositoryImpl.kt: lastQuery = ${lastQuery.value?.query}")
        Log.d("TAG_Max", "")

        if (lastQuery.value != null) {

            Log.d("TAG_Max", "GifsRepositoryImpl.kt: gifs = $gifs")
            Log.d("TAG_Max", "")

            if (gifs.value.isNotEmpty()) {
                emit(
                    Result.Success(
                        gifs.value.take(DEFAULT_AMOUNT_PER_PAGE)
                    )
                )
            } else {

                Log.d("TAG_Max", "GifsRepositoryImpl.kt: can't read gifs from db with last query")
                Log.d("TAG_Max", "")

                emit(Result.Error(DataError.Local.UNKNOWN))
            }
        } else {

            Log.d("TAG_Max", "GifsRepositoryImpl.kt: no queries were at all")
            Log.d("TAG_Max", "")

            emit(Result.Success(emptyList()))
        }
    }

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.handleSameQuery(page: Int) {
        lastQuery.value?.let { query ->
            val startIndex = (page - 1) * DEFAULT_AMOUNT_PER_PAGE
            val lastIndex = startIndex + DEFAULT_AMOUNT_PER_PAGE

            if (startIndex >= gifs.value.size || lastIndex > gifs.value.lastIndex) {

                Log.d("TAG_Max", "GifsRepositoryImpl.kt: load gifs from remote for the page $page")
                Log.d("TAG_Max", "GifsRepositoryImpl.kt: deletedGifIds = ${deletedGifIds.value}")
                Log.d("TAG_Max", "")

                val result = networkManager.loadGifsFromRemote(query = query, offset = startIndex)
                handleNetworkResult(
                    result = result,
                    skipGifsAmount = startIndex
                )
            } else {
                emitPaginatedGifsFromDb(skipGifsAmount = startIndex)
            }

            dbManager.updateCurrentPage(page)
        }
    }

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.handleNewQuery(
        searchingQuery: String,
    ) {
        val offset = 0
        val query = SearchQuery(query = searchingQuery, currentPage = 1)

        Log.d("TAG_Max", "GifsRepositoryImpl.kt: searchingQuery = $searchingQuery")
        Log.d("TAG_Max", "")

        dbManager.saveNewQuery(query)

        Log.d("TAG_Max", "GifsRepositoryImpl.kt: lastQuery = ${lastQuery.value}")
        Log.d("TAG_Max", "")

        val result = networkManager.loadGifsFromRemote(query = query, offset = offset)
        handleNetworkResult(
            result = result,
            skipGifsAmount = offset,
            shouldClearPreviousGifs = true,
        )
    }

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.handleNetworkResult(
        result: Result<List<Gif>, DataError.Network>,
        skipGifsAmount: Int,
        shouldClearPreviousGifs: Boolean = false,
    ) {
        when (result) {
            is Result.Success -> {

                Log.d("TAG_Max", "GifsRepositoryImpl.kt: successful network result!")
                Log.d("TAG_Max", "")

                val filteredGifEntities = result.data
                    .filter { data -> data.id !in deletedGifIds.value }

                if (shouldClearPreviousGifs) dbManager.clearPreviousGifs()

                Log.d(
                    "TAG_Max",
                    "GifsRepositoryImpl.kt: filteredGifEntities = $filteredGifEntities"
                )
                Log.d("TAG_Max", "")

                saveNewGifsIntoDb(
                    filteredGifEntities = filteredGifEntities,
                    skipGifsAmount = skipGifsAmount
                )
            }

            is Result.Error -> {

                Log.d("TAG_Max", "GifsRepositoryImpl.kt: network error result!")
                Log.d("TAG_Max", "")

                emit(Result.Error(result.error))
            }
        }
    }

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.saveNewGifsIntoDb(
        filteredGifEntities: List<Gif>,
        skipGifsAmount: Int,
    ) {
        when (val updatingResult = dbManager.updateGifsInLocalDb(filteredGifEntities)) {
            is Result.Error -> emit(updatingResult)
            is Result.Success -> emitPaginatedGifsFromDb(skipGifsAmount)
        }
    }

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.emitPaginatedGifsFromDb(
        skipGifsAmount: Int,
    ) {

        Log.d(
            "TAG_Max",
            "GifsRepositoryImpl.kt: we have enough gifs in the db for current query and take them"
        )
        Log.d("TAG_Max", "")

        emit(
            Result.Success(
                gifs.value
                    .drop(skipGifsAmount)
                    .take(DEFAULT_AMOUNT_PER_PAGE)
            )
        )
    }

}
