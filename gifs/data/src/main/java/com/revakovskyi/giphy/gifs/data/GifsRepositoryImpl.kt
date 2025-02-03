package com.revakovskyi.giphy.gifs.data

import android.util.Log
import com.revakovskyi.giphy.core.data.local_db.DbManager
import com.revakovskyi.giphy.core.data.network.NetworkManager
import com.revakovskyi.giphy.core.domain.gifs.Gif
import com.revakovskyi.giphy.core.domain.gifs.SearchQuery
import com.revakovskyi.giphy.core.domain.util.DataError
import com.revakovskyi.giphy.core.domain.util.EmptyDataResult
import com.revakovskyi.giphy.core.domain.util.Result
import com.revakovskyi.giphy.gifs.domain.Constants.AMOUNT_TO_DOWNLOAD
import com.revakovskyi.giphy.gifs.domain.Constants.DEFAULT_AMOUNT_ON_PAGE
import com.revakovskyi.giphy.gifs.domain.GifsRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update

class GifsRepositoryImpl(
    private val dbManager: DbManager,
    private val networkManager: NetworkManager,
) : GifsRepository {

    private val lastQuery: StateFlow<SearchQuery> = dbManager.lastQuery
    private val searchQuery = MutableStateFlow(SearchQuery(query = "", currentPage = 1))

    private val pendingQuery = MutableStateFlow<SearchQuery?>(null)
    private val pendingPage = MutableStateFlow<Int?>(null)

    private val pageStartOffset = MutableStateFlow(0)


    override fun observeLastQuery(): Flow<SearchQuery> = lastQuery

    override fun getGifsByQuery(
        query: String,
        page: Int,
    ): Flow<Result<List<Gif>, DataError>> {
        return flow {
            try {

                Log.d("TAG_Max", "GifsRepositoryImpl.kt: getGifsByQuery")
                Log.d("TAG_Max", "GifsRepositoryImpl.kt: query = $query")
                Log.d("TAG_Max", "GifsRepositoryImpl.kt: page = $page")
                Log.d("TAG_Max", "GifsRepositoryImpl.kt: lastQuery = ${lastQuery.value}")
                Log.d("TAG_Max", "")

                if (query == lastQuery.value.query) {
                    when {
                        page > lastQuery.value.currentPage -> handleSameQueryUpperPage(page)
                        page == 1 && page < lastQuery.value.currentPage -> handleFirstPageCase(page)
                        page < lastQuery.value.currentPage -> handleSameQueryLowerPage(page)
                        else -> {

                            Log.d("TAG_Max", "GifsRepositoryImpl.kt: page == 1")
                            Log.d("TAG_Max", "")

                            if (page == 1) handleNewQuery(lastQuery.value.query)
                            else emit(Result.Error(DataError.Local.THE_SAME_DATA))
                        }
                    }
                } else handleNewQuery(query)
            } catch (e: Exception) {
                emit(Result.Error(DataError.Network.UNKNOWN))
            }
        }
    }

    override fun getOriginalGifsByQueryId(queryId: Long): Flow<Result<List<Gif>, DataError.Local>> {
        return dbManager.getGifsByQueryId(queryId)
    }

    override suspend fun deleteGif(gifId: String): EmptyDataResult<DataError.Local> {
        return dbManager.deleteGif(gifId).also { result ->
            if (result is Result.Success) {
                val updatedDeletedCount = lastQuery.value.deletedGifsAmount + 1
                dbManager.updateDeletedGifsAmount(updatedDeletedCount)
            }
        }
    }

    override suspend fun provideNewGif(): Result<Gif, DataError> {
        val neededAmount = 1
        preparePaginationData(lastQuery.value.currentPage + 1)

        val availableGifs = dbManager.checkGifsInLocalDB(
            queryId = lastQuery.value.id,
            limit = neededAmount,
            pageOffset = pageStartOffset.value
        )

        return if (availableGifs.isNotEmpty()) Result.Success(availableGifs.first())
        else downloadNewGif(neededAmount)
    }

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.handleSameQueryUpperPage(page: Int) {

        Log.d("TAG_Max", "GifsRepositoryImpl.kt: handleSameQueryUpperPage")
        Log.d("TAG_Max", "")

        pendingPage.update { page }
        preparePaginationData(page)

        val availableGifs = dbManager.checkGifsInLocalDB(
            queryId = lastQuery.value.id,
            limit = DEFAULT_AMOUNT_ON_PAGE,
            pageOffset = pageStartOffset.value
        )

        if (availableGifs.size >= DEFAULT_AMOUNT_ON_PAGE) {
            emit(Result.Success(availableGifs))
            saveCurrentPage(page)
            return
        }

        fetchGifsAndProvideThemFromDBCache(
            amountToFetchFromRemote = DEFAULT_AMOUNT_ON_PAGE - availableGifs.size,
            amountToGetFromDb = DEFAULT_AMOUNT_ON_PAGE,
            offsetToLoad = calculateStartOffset(availableGifs.size),
            pageOffset = pageStartOffset.value,
            onError = { errorResult -> emit(errorResult) },
            onSuccess = { successResult -> emit(successResult) }
        )
    }

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.handleFirstPageCase(page: Int) {

        Log.d("TAG_Max", "GifsRepositoryImpl.kt: handleFirstPageCase")
        Log.d("TAG_Max", "")

        val successfully = saveCurrentPage(
            page = page,
            onError = { errorResult -> emit(errorResult) }
        )
        if (!successfully) return

        pageStartOffset.update { 0 }

        getGifsFromDatabase(
            queryId = lastQuery.value.id,
            limit = DEFAULT_AMOUNT_ON_PAGE,
            pageOffset = pageStartOffset.value,
            onSuccess = { successResult -> emit(successResult) },
            onError = { errorResult -> emit(errorResult) }
        )
    }

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.handleSameQueryLowerPage(page: Int) {

        Log.d("TAG_Max", "GifsRepositoryImpl.kt: handleSameQueryLowerPage")
        Log.d("TAG_Max", "")

        pendingPage.update { page }
        preparePaginationData(pendingPage.value!!)

        val availableGifs = dbManager.checkGifsInLocalDB(
            queryId = lastQuery.value.id,
            limit = DEFAULT_AMOUNT_ON_PAGE,
            pageOffset = pageStartOffset.value
        )

        if (availableGifs.size >= DEFAULT_AMOUNT_ON_PAGE) {
            emit(Result.Success(availableGifs))
            pendingPage.value?.let { saveCurrentPage(it) }
            resetPendingEntities()
            return
        }

        fetchGifsAndProvideThemFromDBCache(
            amountToFetchFromRemote = DEFAULT_AMOUNT_ON_PAGE - availableGifs.size,
            amountToGetFromDb = DEFAULT_AMOUNT_ON_PAGE,
            offsetToLoad = pageStartOffset.value,
            pageOffset = pageStartOffset.value,
            onError = { errorResult -> emit(errorResult) },
            onSuccess = { successResult -> emit(successResult) }
        )
    }

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.handleNewQuery(query: String) {

        Log.d("TAG_Max", "GifsRepositoryImpl.kt: handleNewQuery")
        Log.d("TAG_Max", "")

        initializeNewQuery(query)

        dbManager.getSearchQueryByQueryText(query)?.let { existingQuery ->
            processExistingQuery(existingQuery)
        } ?: processNewQuery()
    }

    private suspend fun downloadNewGif(neededAmount: Int): Result<Gif, DataError> {
        val deferredResult = CompletableDeferred<Result<Gif, DataError>>()
        pageStartOffset.update { pageStartOffset.value - neededAmount }
        val offsetToLoad = lastQuery.value.maxGifPositionInTable + lastQuery.value.deletedGifsAmount

        fetchGifsAndProvideThemFromDBCache(
            amountToFetchFromRemote = neededAmount,
            amountToGetFromDb = neededAmount,
            offsetToLoad = offsetToLoad,
            pageOffset = pageStartOffset.value,
            onError = { errorResult -> deferredResult.complete(errorResult) },
            onSuccess = { successResult ->
                deferredResult.complete(Result.Success(successResult.data.first()))
            }
        )

        return deferredResult.await()
    }

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.processExistingQuery(
        existingQuery: SearchQuery,
    ) {
        searchQuery.update { existingQuery.copy(currentPage = 1) }

        val availableGifs = dbManager.checkGifsInLocalDB(
            queryId = searchQuery.value.id,
            limit = DEFAULT_AMOUNT_ON_PAGE,
            pageOffset = pageStartOffset.value
        )

        if (availableGifs.size >= DEFAULT_AMOUNT_ON_PAGE) {
            dbManager.saveOrUpdateQuery(searchQuery.value)
            emit(Result.Success(availableGifs))
            return
        }

        fetchGifsAndProvideThemFromDBCache(
            amountToFetchFromRemote = DEFAULT_AMOUNT_ON_PAGE - availableGifs.size,
            amountToGetFromDb = DEFAULT_AMOUNT_ON_PAGE,
            offsetToLoad = calculateStartOffset(availableGifs.size),
            pageOffset = pageStartOffset.value,
            onSuccess = { successResult -> emit(successResult) },
            onError = { errorResult -> emit(errorResult) },
        )
    }

    /**
     * Processes a new search query by saving it to the database, fetching GIFs from the API,
     * and updating the local database accordingly.
     *
     * Steps:
     * 1. Store the last successful query as a pending query to allow rollback if needed.
     * 2. Save the new query to the database to get a new ID.
     * 3. If saving fails, reset the pending state and emit an error.
     * 4. Update the search query with the new ID obtained from the database.
     * 5. Fetch GIFs from the API and attempt to save them to the database.
     * 6. If fetching or saving fails, restore the last successful query and reset the pending state.
     */
    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.processNewQuery() {
        pendingQuery.update { lastQuery.value }

        val savingResult = dbManager.saveOrUpdateQuery(searchQuery.value)

        if (savingResult is Result.Error) {
            resetPendingEntities()
            emit(savingResult)
            return
        }

        searchQuery.update { it.copy(id = lastQuery.value.id) }

        fetchGifsAndProvideThemFromDBCache(
            amountToFetchFromRemote = AMOUNT_TO_DOWNLOAD,
            amountToGetFromDb = DEFAULT_AMOUNT_ON_PAGE,
            offsetToLoad = pageStartOffset.value,
            pageOffset = pageStartOffset.value,
            onSuccess = { successResult -> emit(successResult) },
            onError = { errorResult -> emit(errorResult) },
        )
    }

    private suspend fun fetchGifsAndProvideThemFromDBCache(
        amountToFetchFromRemote: Int,
        amountToGetFromDb: Int,
        offsetToLoad: Int,
        pageOffset: Int,
        onSuccess: suspend (success: Result.Success<List<Gif>>) -> Unit,
        onError: suspend (error: Result.Error<DataError>) -> Unit,
    ) {
        networkManager.fetchGifsFromApi(
            query = lastQuery.value,
            limit = amountToFetchFromRemote,
            offset = offsetToLoad,
            onSuccess = { successResult ->
                saveGifsIntoDb(
                    gifs = successResult.data,
                    onError = { errorResult -> onError(errorResult) },
                    onSuccess = {
                        getGifsFromDatabase(
                            queryId = lastQuery.value.id,
                            limit = amountToGetFromDb,
                            pageOffset = pageOffset,
                            onSuccess = { successResult -> onSuccess(Result.Success(successResult.data)) },
                            onError = { errorResult -> onError(errorResult) }
                        )
                    }
                )
            },
            onError = { errorResult ->
                restoreLastSuccessfulQuery()
                resetPendingEntities()
                onError(errorResult)
            }
        )
    }

    private suspend fun saveGifsIntoDb(
        gifs: List<Gif>,
        onSuccess: suspend () -> Unit,
        onError: suspend (error: Result.Error<DataError.Local>) -> Unit,
    ) {
        when (val savingResult = dbManager.saveGifs(gifs)) {
            is Result.Error -> {
                restoreLastSuccessfulQuery()
                resetPendingEntities()
                onError(savingResult)
            }

            is Result.Success -> {
                pendingPage.value?.let {
                    saveCurrentPage(
                        page = it,
                        onError = { errorResult ->
                            restoreLastSuccessfulQuery()
                            resetPendingEntities()
                            onError(errorResult)
                        }
                    )
                }

                dbManager.markQueryAsSuccessful(searchQuery.value.id)
                dbManager.updateGifsMaxPosition(searchQuery.value.id)

                resetPendingEntities()
                onSuccess()
            }
        }
    }

    private suspend fun getGifsFromDatabase(
        queryId: Long,
        limit: Int,
        pageOffset: Int,
        onSuccess: suspend (success: Result.Success<List<Gif>>) -> Unit,
        onError: suspend (error: Result.Error<DataError.Local>) -> Unit,
    ) {
        val observingResult = dbManager.getGifsByQuery(
            queryId = queryId,
            limit = limit,
            pageOffset = pageOffset
        )

        when (observingResult) {
            is Result.Error -> onError(observingResult)
            is Result.Success -> onSuccess(observingResult)
        }
    }

    private suspend fun saveCurrentPage(
        page: Int,
        onError: suspend (errorResult: Result.Error<DataError.Local>) -> Unit = {},
    ): Boolean {
        val deferredResult = CompletableDeferred<Boolean>()
        dbManager.updateCurrentPage(currentPage = page).also { result ->
            if (result is Result.Error) {
                onError(result)
                deferredResult.complete(false)
            } else deferredResult.complete(true)
        }
        return deferredResult.await()
    }

    private fun preparePaginationData(page: Int) {
        pageStartOffset.update { (page - 1) * DEFAULT_AMOUNT_ON_PAGE }
    }

    private fun calculateStartOffset(availableGifsSize: Int): Int {
        val deletedGifsAmount = lastQuery.value.deletedGifsAmount
        val maxPositionAfterDeleting = lastQuery.value.maxGifPositionInTable

        return if (maxPositionAfterDeleting > 0) maxPositionAfterDeleting + deletedGifsAmount
        else pageStartOffset.value + availableGifsSize
    }

    private fun initializeNewQuery(query: String) {
        pageStartOffset.update { 0 }
        pendingPage.update { 1 }
        searchQuery.update { SearchQuery(query = query, currentPage = pendingPage.value!!) }
    }

    private suspend fun restoreLastSuccessfulQuery() {
        pendingQuery.value?.let { dbManager.saveOrUpdateQuery(it) }
        dbManager.clearUnsuccessfulSearchQueries()
    }

    private fun resetPendingEntities() {
        pendingQuery.update { null }
        pendingPage.update { null }
    }

}
