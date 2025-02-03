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
    private val offsetToLoad = MutableStateFlow(0)
    private val amountToDownload = MutableStateFlow(AMOUNT_TO_DOWNLOAD)


    // TODO: delete it later
    private fun logDebug(vararg messages: String) {
        messages.forEach { Log.d("TAG_Max", "GifsRepositoryImpl.kt: $it") }
        Log.d("TAG_Max", "")
    }


    override fun observeLastQuery(): Flow<SearchQuery> = lastQuery

    override fun getGifsByQuery(
        query: String,
        page: Int,
    ): Flow<Result<List<Gif>, DataError>> {
        return flow {
            logDebug("Start fetching GIFs", "query = $query", "page = $page")

            try {
                if (query == lastQuery.value.query) {
                    when {
                        page > lastQuery.value.currentPage -> handleSameQueryUpperPage(page)
                        page == 1 && page < lastQuery.value.currentPage -> handleFirstPageCase(page)
                        page < lastQuery.value.currentPage -> handleSameQueryLowerPage(page)
                        else -> {
                            logDebug("GifsRepositoryImpl.kt: the same query same page")

                            if (page == 1) handleNewQuery(lastQuery.value.query)
                            else emit(Result.Error(DataError.Local.THE_SAME_DATA))
                        }
                    }
                } else handleNewQuery(query)
            } catch (e: Exception) {
                logDebug("Unexpected error", "Message = ${e.localizedMessage}")
                emit(Result.Error(DataError.Network.UNKNOWN))
            }
        }
    }

    override fun getOriginalGifsByQueryId(queryId: Long): Flow<Result<List<Gif>, DataError.Local>> {
        return dbManager.getGifsByQueryId(queryId)
    }

    override suspend fun deleteGif(gifId: String): EmptyDataResult<DataError.Local> {

        logDebug(
            "deleteGif",
            "gifId = $gifId",
        )

        val deletingResult = dbManager.deleteGif(gifId)

        if (deletingResult is Result.Success) {

            logDebug(
                "deletingResult Success - updateDeletedGifsCount",
                "lastQuery = ${lastQuery.value}",
            )

            dbManager.updateDeletedGifsAmount(
                deletedGifsAmount = lastQuery.value.deletedGifsAmount + 1
            )
        }

        return deletingResult
    }

    override suspend fun provideNewGif(): Result<Gif, DataError> {
        val deferredResult = CompletableDeferred<Result<Gif, DataError>>()
        val neededAmount = 1

        // for the page 2 pageStartOffset = 48
        preparePaginationData(lastQuery.value.currentPage + 1)

        val availableGifsForNextPage = checkGifsInLocalDB(
            queryId = lastQuery.value.id,
            gifsAmount = neededAmount,
        )

        logDebug(
            "Success availableGifsForNextPage = $availableGifsForNextPage",
            "Success size = ${availableGifsForNextPage.size}"
        )

        if (availableGifsForNextPage.size < neededAmount) {
            searchQuery.update { lastQuery.value }

            val pageStartOffset = pageStartOffset.value - neededAmount
            val deletedGifsAmount = lastQuery.value.deletedGifsAmount
            val maxGiPositionInTable = lastQuery.value.maxGifPositionInTable

            val actualStartOffsetToLoad = maxGiPositionInTable + deletedGifsAmount

            offsetToLoad.update { actualStartOffsetToLoad }

            logDebug(
                "before loading",
                "pageStartOffset = $pageStartOffset",
                "deletedGifsAmount = $deletedGifsAmount",
                "maxGiPositionInTable = $maxGiPositionInTable",
                "neededAmount = $neededAmount",
                "offsetToLoad = ${offsetToLoad.value}",
            )


            loadMoreGifsFromRemote(
                amount = neededAmount,
                offset = offsetToLoad.value,
                onSuccess = { successResult ->
                    saveGifsIntoDb(
                        gifs = successResult.data,
                        onError = { errorResult ->
                            deferredResult.complete(errorResult)
                        },
                        onSuccess = {
                            getGifsFromDatabase(
                                queryId = lastQuery.value.id,
                                limit = neededAmount,
                                pageOffset = pageStartOffset,
                                onSuccess = { successResult ->
                                    val gif = successResult.data.first()
                                    deferredResult.complete(Result.Success(gif))
                                },
                                onError = { errorResult -> deferredResult.complete(errorResult) }
                            )
                        }
                    )
                },
                onError = { errorResult ->
                    restoreLastSuccessfulQuery()
                    resetPendingEntities()
                    deferredResult.complete(errorResult)
                }
            )
        } else {
            logDebug("Enough gifs found, using DB results.")
            deferredResult.complete(Result.Success(availableGifsForNextPage.first()))
        }
        return deferredResult.await()
    }

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.handleSameQueryUpperPage(page: Int) {
        pendingPage.update { page }

        logDebug(
            "Handling same query upper page",
            "previousPage = ${lastQuery.value.currentPage}",
        )

        preparePaginationData(page)

        val availableGifsForNextPage = checkGifsInLocalDB(lastQuery.value.id)

        logDebug(
            "Success availableGifsForNextPage = $availableGifsForNextPage",
            "Success size = ${availableGifsForNextPage.size}"
        )

        if (availableGifsForNextPage.size < DEFAULT_AMOUNT_ON_PAGE) {
            searchQuery.update { lastQuery.value }

            val availableGifsForNextPageSize = availableGifsForNextPage.size
            val amount = DEFAULT_AMOUNT_ON_PAGE - availableGifsForNextPageSize

            val pageStartOffset = pageStartOffset.value
            val deletedGifsAmount = lastQuery.value.deletedGifsAmount
            val maxPositionAfterDeleting = lastQuery.value.maxGifPositionInTable

            val actualStartOffsetToLoad = if (maxPositionAfterDeleting > 0) {
                maxPositionAfterDeleting + deletedGifsAmount
            } else pageStartOffset + availableGifsForNextPageSize

            offsetToLoad.update { actualStartOffsetToLoad }

            logDebug(
                "before loading",
                "availableGifsForNextPageSize = $availableGifsForNextPageSize",
                "pageStartOffset = $pageStartOffset",
                "deletedGifsAmount = $deletedGifsAmount",
                "maxPositionAfterDeleting = $maxPositionAfterDeleting",
                "amount = $amount",
                "offsetToLoad = ${offsetToLoad.value}",
            )

            loadMoreGifsFromRemote(
                amount = amount,
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
                                pageOffset = this@GifsRepositoryImpl.pageStartOffset.value,
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
            logDebug("Enough gifs found, using DB results.")
            emit(Result.Success(availableGifsForNextPage))
            saveCurrentPage(page = page)
        }
    }

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.handleFirstPageCase(page: Int) {
        logDebug("Opening first page for last query")

        val successfully = saveCurrentPage(
            page = page,
            onError = { errorResult ->
                emit(errorResult)
            }
        )
        if (!successfully) return


        pageStartOffset.update { 0 }

        logDebug(
            "Updated lastQuery",
            "currentPage = $page",
            "lastQuery = ${lastQuery.value}"
        )

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

        logDebug(
            "Handling same query lower page",
            "previousPage = ${lastQuery.value.currentPage}",
            "currentPage = ${pendingPage.value}"
        )

        preparePaginationData(pendingPage.value!!)

        val gifs = checkGifsInLocalDB(lastQuery.value.id)

        logDebug(
            "Success gifs = $gifs",
            "Success size = ${gifs.size}"
        )

        if (gifs.size < DEFAULT_AMOUNT_ON_PAGE) {
            loadMoreGifsFromRemote(
                amount = DEFAULT_AMOUNT_ON_PAGE - gifs.lastIndex,
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
            logDebug("Enough gifs found, using DB results.")
            emit(Result.Success(gifs))
            pendingPage.value?.let { saveCurrentPage(it) }
            resetPendingEntities()
        }
    }

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.handleNewQuery(query: String) {

        logDebug(
            "Handling new query",
            "searchingQuery = $query"
        )

        initializeNewQuery(query)
        val existingQuery = dbManager.getSearchQueryByQueryText(query)

        logDebug("existingQuery = $existingQuery")

        if (existingQuery != null) processExistingQuery(existingQuery)
        else processNewQuery()
    }

    private fun preparePaginationData(page: Int) {
        pageStartOffset.update { (page - 1) * DEFAULT_AMOUNT_ON_PAGE }

        logDebug(
            "currentPage = ${pendingPage.value}",
            "pageOffset = ${pageStartOffset.value}",
        )
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

//    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.checkGifsInLocalDB(
//        queryId: Long,
//    ): List<Gif> {
//        val observingResult = dbManager.observeGifsFromDbByQuery(
//            queryId = queryId,
//            gifsAmount = DEFAULT_AMOUNT_ON_PAGE,
//            pageOffset = pageStartOffset.value
//        )
//
//        return when (observingResult) {
//            is Result.Error -> {
//                emit(observingResult)
//                emptyList()
//            }
//
//            is Result.Success -> observingResult.data
//        }
//    }

    private suspend fun loadMoreGifsFromRemote(
        amount: Int,
        offset: Int,
        onSuccess: suspend (success: Result.Success<List<Gif>>) -> Unit,
        onError: suspend (error: Result.Error<DataError.Network>) -> Unit,
    ) {
        amountToDownload.update { amount }

        logDebug(
            "Not enough gifs, loading more from network...",
            "searchQuery = ${searchQuery.value}",
            "loadingOffset = $offset",
            "amountToDownload = $amount"
        )

        val result = networkManager.fetchGifsFromApi(
            query = searchQuery.value,
            offset = offset,
            limit = amount,
        )

        when (result) {
            is Result.Success -> {
                logDebug(
                    "Successful network result!",
                    "Network GIFs count = ${result.data.size}",
                    "Unique GIFs = ${result.data.distinctBy { it.id }.size}"
                )

                result.data.forEach {
                    Log.d("TAG_Max", "GifsRepositoryImpl.kt: gifId = ${it.id}")
                }
                Log.d("TAG_Max", "")

                onSuccess(result)
            }

            is Result.Error -> {
                logDebug(
                    "Network error result!",
                    "Delete pending query from db (it was absolutely new search query)"
                )

                onError(result)
            }
        }
    }

    private suspend fun saveCurrentPage(
        page: Int,
        onError: suspend (errorResult: Result.Error<DataError.Local>) -> Unit = {},
    ): Boolean {
        val deferredResult = CompletableDeferred<Boolean>()

        dbManager.updateCurrentPage(currentPage = page).also { result ->
            if (result is Result.Error) {
                logDebug("Failed to update page in DB", "currentPage = $page")
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

        logDebug(
            "Fetching GIFs from DB",
            "queryId = $queryId",
            "gifsAmount = $limit",
            "pageOffset = $pageOffset"
        )

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
        amountToDownload.update { AMOUNT_TO_DOWNLOAD }
        searchQuery.update { SearchQuery(query = query, currentPage = pendingPage.value!!) }
        logDebug("Initialized new query", "query = ${searchQuery.value}")
    }

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.processExistingQuery(
        existingQuery: SearchQuery,
    ) {
        logDebug("query exists in DB, reusing it")

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

            logDebug(
                "before loading",
                "availableGifsForNextPageSize = $availableGifsForNextPageSize",
                "pageStartOffset = $pageStartOffset",
                "deletedGifsAmount = $deletedGifsAmount",
                "maxPositionAfterDeleting = $maxPositionAfterDeleting",
                "amount = $amount",
                "offsetToLoad = ${offsetToLoad.value}",
            )

            loadMoreGifsFromRemote(
                amount = amount,
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
            logDebug(
                "Enough saved gifs in DB, using them",
                "pendingQuery = ${pendingQuery.value}",
                "pendingPage = ${pendingPage.value}",
                "lastQuery = ${lastQuery.value}",
                "searchQuery = ${searchQuery.value}",
            )

            dbManager.saveOrUpdateQuery(searchQuery.value)
            emit(Result.Success(gifs))
        }
    }

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.processNewQuery() {
        // TODO: update a pending query with last one to use it for deleting a new saved one from db if smth goes wrong
        pendingQuery.update { lastQuery.value }
        logDebug("update pendingQuery - ${pendingQuery.value}")

        // TODO: 1 - save new query to get a new id for query
        val savingResult = dbManager.saveOrUpdateQuery(searchQuery.value)

        if (savingResult is Result.Error) {
            logDebug("Error saving new query", "error = ${savingResult.error.name}")
            resetPendingEntities()
            emit(savingResult)
            return
        }

        logDebug("Successfully save NewSearchQuery", "queryId = ${lastQuery.value}")

        // TODO: after we save new query - last query update and here we get a new id from db for the search query
        searchQuery.update { it.copy(id = lastQuery.value.id) }

        loadMoreGifsFromRemote(
            amount = AMOUNT_TO_DOWNLOAD,
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
                logDebug("Error saving GIFs to DB", "error = ${savingResult.error.name}")

                restoreLastSuccessfulQuery()
                resetPendingEntities()
                onError(savingResult)
            }

            is Result.Success -> {
                logDebug("GIFs saved successfully")

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

                logDebug(
                    "Successfully update page in DB",
                    "lastQuery = ${lastQuery.value}"
                )

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
