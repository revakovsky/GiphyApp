package com.revakovskyi.giphy.gifs.data

import android.util.Log
import com.revakovskyi.giphy.core.data.local_db.DbManager
import com.revakovskyi.giphy.core.data.network.NetworkManager
import com.revakovskyi.giphy.core.domain.gifs.Gif
import com.revakovskyi.giphy.core.domain.gifs.SearchQuery
import com.revakovskyi.giphy.core.domain.util.DataError
import com.revakovskyi.giphy.core.domain.util.Result
import com.revakovskyi.giphy.gifs.domain.Constants.AMOUNT_TO_DOWNLOAD
import com.revakovskyi.giphy.gifs.domain.Constants.DEFAULT_AMOUNT_ON_PAGE
import com.revakovskyi.giphy.gifs.domain.GifsRepository
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

    private val pageOffset = MutableStateFlow(0)
    private val amountToDownload = MutableStateFlow(AMOUNT_TO_DOWNLOAD)


    // TODO: delete it later
    private fun logDebug(vararg messages: String) {
        messages.forEach { Log.d("TAG_Max", "GifsRepositoryImpl.kt: $it") }
        Log.d("TAG_Max", "")
    }


    override fun isDbEmpty(): Flow<Boolean> = dbManager.isDbEmpty()
    override fun observeLastQuery(): Flow<SearchQuery> = lastQuery

    override fun fetchGifsByRequest(
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
                            handleNewQuery(lastQuery.value.query)
                        }
                    }
                } else handleNewQuery(query)
            } catch (e: Exception) {
                logDebug("Unexpected error", "Message = ${e.localizedMessage}")
                emit(Result.Error(DataError.Network.UNKNOWN))
            }
        }
    }

    override fun getGifsByQueryId(queryId: Long): Flow<Result<List<Gif>, DataError.Local>> {
        return dbManager.getGifsByQueryId(queryId)
    }

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.handleSameQueryUpperPage(page: Int) {
        pendingPage.update { page }

        logDebug(
            "Handling same query upper page",
            "previousPage = ${lastQuery.value.currentPage}",
            "currentPage = ${pendingPage.value}"
        )

        preparePaginationData(page)

        val gifs = checkGifsInLocalDB(lastQuery.value.id)

        logDebug(
            "Success gifs = $gifs",
            "Success size = ${gifs.size}"
        )

        if (gifs.size < DEFAULT_AMOUNT_ON_PAGE) {
            searchQuery.update { lastQuery.value }
            loadMoreGifsFromRemote(gifs)
        } else {
            logDebug("Enough gifs found, using DB results.")
            emit(Result.Success(gifs))
            if (saveCurrentPage(page) is Result.Error) return
        }
    }

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.handleFirstPageCase(page: Int) {
        logDebug("Opening first page for last query")

        if (saveCurrentPage(page) is Result.Error) return

        pageOffset.update { 0 }
        logDebug("Updated lastQuery", "currentPage = $page", "lastQuery = ${lastQuery.value}")

        fetchGifsFromDB(
            queryId = lastQuery.value.id,
            gifsAmount = DEFAULT_AMOUNT_ON_PAGE,
            pageOffset = pageOffset.value,
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

        if (gifs.size < DEFAULT_AMOUNT_ON_PAGE) loadMoreGifsFromRemote(gifs)
        else {
            logDebug("Enough gifs found, using DB results.")
            emit(Result.Success(gifs))
            pendingPage.value?.let { saveCurrentPage(it) }
            resetPendingEntities()
        }
    }

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.handleNewQuery(query: String) {
        logDebug("Handling new query", "searchingQuery = $query")

        initializeNewQuery(query)
        val existingQuery = dbManager.getQueryByText(query)

        logDebug("existingQuery = $existingQuery")

        if (existingQuery != null) processExistingQuery(existingQuery)
        else processNewQuery()
    }

    private fun preparePaginationData(page: Int) {
        pageOffset.update { (page - 1) * DEFAULT_AMOUNT_ON_PAGE }
        logDebug(
            "lastQuery = ${lastQuery.value}",
            "pageOffset = ${pageOffset.value}",
        )
    }

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.checkGifsInLocalDB(
        queryId: Long,
    ): List<Gif> {
        val observingResult = dbManager.observeGifsFromDbByQuery(
            queryId = queryId,
            gifsAmount = DEFAULT_AMOUNT_ON_PAGE,
            pageOffset = pageOffset.value
        )

        return when (observingResult) {
            is Result.Error -> {
                emit(observingResult)
                emptyList()
            }

            is Result.Success -> observingResult.data
        }
    }

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.loadMoreGifsFromRemote(gifs: List<Gif>) {
        amountToDownload.update { DEFAULT_AMOUNT_ON_PAGE - gifs.lastIndex }

        logDebug(
            "Not enough gifs, loading more from network...",
            "searchQuery = ${searchQuery.value}",
            "pageOffset = ${pageOffset.value}",
            "amountToDownload = ${amountToDownload.value}"
        )

        val result = networkManager.loadGifsFromRemote(
            query = searchQuery.value,
            offset = pageOffset.value,
            amountToDownload = amountToDownload.value,
        )

        handleNetworkResult(result = result)
    }

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.saveCurrentPage(
        page: Int,
    ): Result<Unit, DataError.Local> {
        return dbManager.saveCurrentPage(
            queryId = lastQuery.value.id,
            currentPage = page
        ).also { result ->
            if (result is Result.Error) {
                logDebug("Failed to update page in DB", "currentPage = $page")
                emit(result)
            }
        }
    }

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.handleNetworkResult(
        result: Result<List<Gif>, DataError.Network>,
    ) {
        when (result) {
            is Result.Success -> {
                logDebug(
                    "Successful network result!",
                    "Network GIFs count = ${result.data.size}",
                    "Unique GIFs = ${result.data.distinctBy { it.id }.size}"
                )
                saveGifsIntoDb(gifs = result.data)
            }

            is Result.Error -> {
                logDebug(
                    "Network error result!",
                    "Delete pending query from db (it was absolutely new search query)"
                )
                restoreLastSuccessfulQuery()
                resetPendingEntities()
                emit(result)
            }
        }
    }

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.fetchGifsFromDB(
        queryId: Long,
        gifsAmount: Int,
        pageOffset: Int,
    ) {
        logDebug(
            "Fetching GIFs from DB",
            "queryId = $queryId",
            "gifsAmount = $gifsAmount",
            "pageOffset = $pageOffset"
        )
        val observingResult = dbManager.observeGifsFromDbByQuery(
            queryId = queryId,
            gifsAmount = gifsAmount,
            pageOffset = pageOffset
        )
        emit(observingResult)
    }

    private fun initializeNewQuery(query: String) {
        pageOffset.update { 0 }
        pendingPage.update { 1 }
        amountToDownload.update { AMOUNT_TO_DOWNLOAD }
        searchQuery.update { SearchQuery(query = query, currentPage = pendingPage.value!!) }
        logDebug("Initialized new query", "query = ${searchQuery.value}")
    }

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.processExistingQuery(
        existingQuery: SearchQuery,
    ) {
        logDebug("query exists in DB, reusing it")
        searchQuery.update { it.copy(id = existingQuery.id) }

        val gifs = checkGifsInLocalDB(existingQuery.id)

        if (gifs.size < DEFAULT_AMOUNT_ON_PAGE) loadMoreGifsFromRemote(gifs)
        else {
            logDebug("Enough saved gifs in DB, using them")
            emit(Result.Success(gifs))
            pendingPage.value?.let { saveCurrentPage(it) }
        }
    }

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.processNewQuery() {
        // TODO: update a pending query with last one to use it for deleting a new saved one from db if smth goes wrong
        pendingQuery.update { lastQuery.value }
        logDebug("update pendingQuery - ${pendingQuery.value}")

        // TODO: 1 - save new query to get a new id for query
        val savingResult = dbManager.saveNewQuery(searchQuery.value)

        if (savingResult is Result.Error) {
            logDebug("Error saving new query", "error = ${savingResult.error.name}")
            resetPendingEntities()
            emit(savingResult)
            return
        }

        logDebug("Successfully save NewSearchQuery", "queryId = ${lastQuery.value}")

        // TODO: after we save new query - last query update and here we get a new id from db for the search query
        searchQuery.update { it.copy(id = lastQuery.value.id) }

        loadMoreGifsFromRemote(emptyList())
    }

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.saveGifsIntoDb(
        gifs: List<Gif>,
    ) {
        val savingResult = saveGifs(gifs)
        if (savingResult is Result.Error) {
            restoreLastSuccessfulQuery()
            resetPendingEntities()
            return
        }

        pendingPage.value?.let {
            val result = saveCurrentPage(it)
            if (result is Result.Error) {
                restoreLastSuccessfulQuery()
                resetPendingEntities()
                return
            }
        }

        dbManager.markQueryAsSuccessful(searchQuery.value.id)

        resetPendingEntities()

        logDebug(
            "Successfully update page in DB",
            "lastQuery = ${lastQuery.value}"
        )

        fetchGifsFromDB(
            queryId = lastQuery.value.id,
            gifsAmount = DEFAULT_AMOUNT_ON_PAGE,
            pageOffset = pageOffset.value,
        )
    }

    private suspend fun restoreLastSuccessfulQuery() {
        pendingQuery.value?.let { dbManager.saveNewQuery(it) }
        dbManager.clearUnsuccessfulSearchQueries()
    }

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.saveGifs(
        gifs: List<Gif>,
    ): Result<Unit, DataError.Local> {
        return dbManager.saveGifs(gifs).also { result ->
            if (result is Result.Error) {
                logDebug("Error saving GIFs to DB", "error = ${result.error.name}")
                emit(result)
            } else {
                logDebug("GIFs saved successfully")
            }
        }
    }

    private fun resetPendingEntities() {
        pendingQuery.update { null }
        pendingPage.update { null }
    }

}
