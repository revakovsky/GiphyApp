package com.revakovskyi.giphy.gifs.data

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
    private val offsetToLoad = MutableStateFlow(0)


    override fun observeLastQuery(): Flow<SearchQuery> = lastQuery

    override fun getGifsByQuery(
        query: String,
        page: Int,
    ): Flow<Result<List<Gif>, DataError>> {
        return flow {
            try {
                if (query == lastQuery.value.query) {
                    when {
                        page > lastQuery.value.currentPage -> handleSameQueryUpperPage(page)
                        page == 1 && page < lastQuery.value.currentPage -> handleFirstPageCase(page)
                        page < lastQuery.value.currentPage -> handleSameQueryLowerPage(page)
                        else -> {
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

        val availableGifs = checkGifsInLocalDB(lastQuery.value.id, neededAmount)

        return if (availableGifs.isNotEmpty()) Result.Success(availableGifs.first())
        else downloadNewGif(neededAmount)
    }

    private suspend fun downloadNewGif(neededAmount: Int): Result<Gif, DataError> {
        val deferredResult = CompletableDeferred<Result<Gif, DataError>>()
        pageStartOffset.update { pageStartOffset.value - neededAmount }

        val offsetToLoad = lastQuery.value.maxGifPositionInTable + lastQuery.value.deletedGifsAmount

        fetchAndSaveGifs(
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

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.handleSameQueryUpperPage(page: Int) {
        pendingPage.update { page }
        preparePaginationData(page)

        val availableGifs = checkGifsInLocalDB(lastQuery.value.id)

        if (availableGifs.size >= DEFAULT_AMOUNT_ON_PAGE) {
            emit(Result.Success(availableGifs))
            saveCurrentPage(page)
            return
        }

        fetchAndSaveGifs(
            amountToFetchFromRemote = DEFAULT_AMOUNT_ON_PAGE - availableGifs.size,
            amountToGetFromDb = DEFAULT_AMOUNT_ON_PAGE,
            offsetToLoad = calculateStartOffset(availableGifs.size),
            pageOffset = pageStartOffset.value,
            onError = { errorResult -> emit(errorResult) },
            onSuccess = { successResult -> emit(Result.Success(successResult.data)) }
        )
    }

    private fun calculateStartOffset(availableGifsSize: Int): Int {
        val deletedGifsAmount = lastQuery.value.deletedGifsAmount
        val maxPositionAfterDeleting = lastQuery.value.maxGifPositionInTable

        return if (maxPositionAfterDeleting > 0) maxPositionAfterDeleting + deletedGifsAmount
        else pageStartOffset.value + availableGifsSize
    }

    private suspend fun fetchAndSaveGifs(
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

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.handleFirstPageCase(page: Int) {
        val successfully = saveCurrentPage(
            page = page,
            onError = { errorResult ->
                emit(errorResult)
            }
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
        pendingPage.update { page }

        preparePaginationData(pendingPage.value!!)

        val gifs = checkGifsInLocalDB(lastQuery.value.id)

        if (gifs.size < DEFAULT_AMOUNT_ON_PAGE) {
            networkManager.fetchGifsFromApi(
                query = searchQuery.value,
                limit = DEFAULT_AMOUNT_ON_PAGE - gifs.lastIndex,
                offset = pageStartOffset.value,
                onSuccess = { successResult ->
                    saveGifsIntoDb(
                        gifs = successResult.data,
                        onError = { errorResult ->
                            emit(errorResult)
                        },
                        onSuccess = {
                            getGifsFromDatabase(
                                queryId = lastQuery.value.id,
                                limit = DEFAULT_AMOUNT_ON_PAGE,
                                pageOffset = pageStartOffset.value,
                                onSuccess = { successResult -> emit(successResult) },
                                onError = { errorResult -> emit(errorResult) }
                            )
                        }
                    )
                },
                onError = { errorResult ->
                    restoreLastSuccessfulQuery()
                    resetPendingEntities()
                    emit(errorResult)
                }
            )
        } else {
            emit(Result.Success(gifs))
            pendingPage.value?.let { saveCurrentPage(it) }
            resetPendingEntities()
        }
    }

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.handleNewQuery(query: String) {
        initializeNewQuery(query)
        val existingQuery = dbManager.getSearchQueryByQueryText(query)

        if (existingQuery != null) processExistingQuery(existingQuery)
        else processNewQuery()
    }

    private fun preparePaginationData(page: Int) {
        pageStartOffset.update { (page - 1) * DEFAULT_AMOUNT_ON_PAGE }
    }

    private suspend fun checkGifsInLocalDB(
        queryId: Long,
        gifsAmount: Int = DEFAULT_AMOUNT_ON_PAGE,
    ): List<Gif> {
        val observingResult = dbManager.getGifsByQuery(
            queryId = queryId,
            limit = gifsAmount,
            pageOffset = pageStartOffset.value
        )

        return when (observingResult) {
            is Result.Error -> emptyList()
            is Result.Success -> observingResult.data
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

    private fun initializeNewQuery(query: String) {
        pageStartOffset.update { 0 }
        pendingPage.update { 1 }
        searchQuery.update { SearchQuery(query = query, currentPage = pendingPage.value!!) }
    }

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.processExistingQuery(
        existingQuery: SearchQuery,
    ) {
        searchQuery.update { existingQuery }

        val gifs = checkGifsInLocalDB(searchQuery.value.id)

        if (gifs.size < DEFAULT_AMOUNT_ON_PAGE) {
            searchQuery.update { lastQuery.value }

            preparePaginationData(lastQuery.value.currentPage + 1)

            val availableGifsForNextPageSize = gifs.size
            val amount = DEFAULT_AMOUNT_ON_PAGE - availableGifsForNextPageSize

            val pageStartOffset = pageStartOffset.value
            val deletedGifsAmount = lastQuery.value.deletedGifsAmount
            val maxPositionAfterDeleting = lastQuery.value.maxGifPositionInTable

            val actualStartOffsetToLoad = if (maxPositionAfterDeleting > 0) {
                maxPositionAfterDeleting + deletedGifsAmount
            } else pageStartOffset + availableGifsForNextPageSize

            offsetToLoad.update { actualStartOffsetToLoad }

            networkManager.fetchGifsFromApi(
                query = searchQuery.value,
                limit = amount,
                offset = offsetToLoad.value,
                onSuccess = { successResult ->
                    saveGifsIntoDb(
                        gifs = successResult.data,
                        onError = { errorResult ->
                            emit(errorResult)
                        },
                        onSuccess = {
                            getGifsFromDatabase(
                                queryId = lastQuery.value.id,
                                limit = DEFAULT_AMOUNT_ON_PAGE,
                                pageOffset = pageStartOffset,
                                onSuccess = { successResult -> emit(successResult) },
                                onError = { errorResult -> emit(errorResult) }
                            )
                        }
                    )
                },
                onError = { errorResult ->
                    restoreLastSuccessfulQuery()
                    resetPendingEntities()
                    emit(errorResult)
                }
            )
        } else {
            dbManager.saveOrUpdateQuery(searchQuery.value)
            emit(Result.Success(gifs))
        }
    }

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.processNewQuery() {
        // TODO: update a pending query with last one to use it for deleting a new saved one from db if smth goes wrong
        pendingQuery.update { lastQuery.value }

        // TODO: 1 - save new query to get a new id for query
        val savingResult = dbManager.saveOrUpdateQuery(searchQuery.value)

        if (savingResult is Result.Error) {
            resetPendingEntities()
            emit(savingResult)
            return
        }

        // TODO: after we save new query - last query update and here we get a new id from db for the search query
        searchQuery.update { it.copy(id = lastQuery.value.id) }

        networkManager.fetchGifsFromApi(
            query = searchQuery.value,
            limit = AMOUNT_TO_DOWNLOAD,
            offset = pageStartOffset.value,
            onSuccess = { successResult ->
                saveGifsIntoDb(
                    gifs = successResult.data,
                    onError = { errorResult ->
                        emit(errorResult)
                    },
                    onSuccess = {
                        getGifsFromDatabase(
                            queryId = lastQuery.value.id,
                            limit = DEFAULT_AMOUNT_ON_PAGE,
                            pageOffset = pageStartOffset.value,
                            onSuccess = { successResult -> emit(successResult) },
                            onError = { errorResult -> emit(errorResult) }
                        )
                    }
                )
            },
            onError = { errorResult ->
                restoreLastSuccessfulQuery()
                resetPendingEntities()
                emit(errorResult)
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

    private suspend fun restoreLastSuccessfulQuery() {
        pendingQuery.value?.let { dbManager.saveOrUpdateQuery(it) }
        dbManager.clearUnsuccessfulSearchQueries()
    }

    private fun resetPendingEntities() {
        pendingQuery.update { null }
        pendingPage.update { null }
    }

}
