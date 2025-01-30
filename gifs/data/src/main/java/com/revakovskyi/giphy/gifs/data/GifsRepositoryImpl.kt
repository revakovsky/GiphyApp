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
    private val currentQuery = MutableStateFlow(SearchQuery(query = "", currentPage = 1))

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
        searchingQuery: String,
        page: Int,
    ): Flow<Result<List<Gif>, DataError>> {
        return flow {
            logDebug("Start fetching GIFs", "searchingQuery = $searchingQuery", "page = $page")

            try {
                if (searchingQuery == lastQuery.value.query) {
                    when {
                        page > lastQuery.value.currentPage -> handleSameQueryUpperPage(page)
                        page == 1 && page < lastQuery.value.currentPage -> handleFirstPageCase(page)
                        page < lastQuery.value.currentPage -> handleSameQueryLowerPage(page)
                        else -> {
                            logDebug("GifsRepositoryImpl.kt: the same query same page")
                            handleNewQuery(lastQuery.value.query)
                        }
                    }
                } else handleNewQuery(searchingQuery)
            } catch (e: Exception) {
                logDebug("Unexpected error", "Message = ${e.localizedMessage}")
                emit(Result.Error(DataError.Network.UNKNOWN))
            }
        }
    }

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.handleSameQueryUpperPage(page: Int) {
        logDebug(
            "Handling same query upper page",
            "previousPage = ${lastQuery.value.currentPage}",
            "currentPage = $page"
        )

        preparePaginationData(page)

        logDebug(
            "lastQuery = ${lastQuery.value}",
            "pageOffset = ${pageOffset.value}",
        )

        val gifs = checkGifsInLocalDB(lastQuery.value.id)

        logDebug(
            "Success gifs = $gifs",
            "Success size = ${gifs.size}"
        )

        if (gifs.size < DEFAULT_AMOUNT_ON_PAGE) {
            loadMoreGifsFromRemote(gifs)
        } else {
            logDebug("Enough gifs found, using DB results.")
            emit(Result.Success(gifs))
        }

        if (saveCurrentPage(page) is Result.Error) return
    }

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.handleFirstPageCase(page: Int) {
        logDebug("Opening first page for last query")

        if (saveCurrentPage(page) is Result.Error) return

        pageOffset.value = 0
        logDebug("Updated lastQuery", "currentPage = $page", "lastQuery = ${lastQuery.value}")

        fetchGifsFromDB(
            queryId = lastQuery.value.id,
            gifsAmount = DEFAULT_AMOUNT_ON_PAGE,
            pageOffset = pageOffset.value,
        )
    }

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.handleSameQueryLowerPage(page: Int) {
        logDebug(
            "Handling same query lower page",
            "previousPage = ${lastQuery.value.currentPage}",
            "currentPage = $page"
        )

        if (saveCurrentPage(page) is Result.Error) return

        preparePaginationData(page)

        val gifs = checkGifsInLocalDB(lastQuery.value.id)

        logDebug(
            "Success gifs = $gifs",
            "Success size = ${gifs.size}"
        )

        if (gifs.size < DEFAULT_AMOUNT_ON_PAGE) loadMoreGifsFromRemote(gifs)
        else {
            logDebug("Enough gifs found, using DB results.")
            emit(Result.Success(gifs))
        }
    }

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.handleNewQuery(
        searchingQuery: String,
    ) {
        logDebug("Handling new query", "searchingQuery = $searchingQuery")

        initializeNewQuery(searchingQuery)
        val existingQuery = dbManager.getQueryByText(searchingQuery)

        logDebug("existingQuery = $existingQuery")

        if (existingQuery != null) processExistingQuery(existingQuery)
        else processNewQuery()
    }

    private fun preparePaginationData(page: Int) {
        pageOffset.value = (page - 1) * DEFAULT_AMOUNT_ON_PAGE
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
        amountToDownload.value = DEFAULT_AMOUNT_ON_PAGE - gifs.lastIndex

        logDebug(
            "Not enough gifs, loading more from network...",
            "lastQuery = ${lastQuery.value}",
            "pageOffset = ${pageOffset.value}",
            "amountToDownload = ${amountToDownload.value}"
        )

        val result = networkManager.loadGifsFromRemote(
            query = lastQuery.value,
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
                logDebug("Network error result!")
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

    private fun initializeNewQuery(searchingQuery: String) {
        pageOffset.value = 0
        amountToDownload.value = AMOUNT_TO_DOWNLOAD
        currentQuery.value = SearchQuery(query = searchingQuery, currentPage = 1)
        logDebug("Initialized new query", "query = ${currentQuery.value}")
    }

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.processExistingQuery(
        existingQuery: SearchQuery,
    ) {
        logDebug("query exists in DB, reusing it")
        currentQuery.update { it.copy(id = existingQuery.id) }

        val gifs = checkGifsInLocalDB(existingQuery.id)

        if (gifs.size < DEFAULT_AMOUNT_ON_PAGE) loadMoreGifsFromRemote(gifs)
        else {
            logDebug("Enough saved gifs in DB, using them")
            emit(Result.Success(gifs))
        }
    }

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.processNewQuery() {
        val savingResult = dbManager.saveNewQuery(currentQuery.value)

        if (savingResult is Result.Error) {
            logDebug("Error saving new query", "error = ${savingResult.error.name}")
            emit(savingResult)
            return
        }

        logDebug("Successfully saved new query", "queryId = ${lastQuery.value.id}")
        currentQuery.update { it.copy(id = lastQuery.value.id) }
        loadMoreGifsFromRemote(emptyList())
    }

    private suspend fun FlowCollector<Result<List<Gif>, DataError>>.saveGifsIntoDb(
        gifs: List<Gif>,
    ) {
        val savingResult = saveGifs(gifs)
        if (savingResult is Result.Error) return

        fetchGifsFromDB(
            queryId = lastQuery.value.id,
            gifsAmount = DEFAULT_AMOUNT_ON_PAGE,
            pageOffset = pageOffset.value,
        )
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

}
