package com.revakovskyi.giphy.core.data.network

import com.revakovskyi.giphy.core.data.mapper.toDomain
import com.revakovskyi.giphy.core.data.utils.safeCall
import com.revakovskyi.giphy.core.domain.connectivity.ConnectivityObserver
import com.revakovskyi.giphy.core.domain.connectivity.InternetStatus
import com.revakovskyi.giphy.core.domain.gifs.Gif
import com.revakovskyi.giphy.core.domain.gifs.SearchQuery
import com.revakovskyi.giphy.core.domain.util.DataError
import com.revakovskyi.giphy.core.domain.util.Result
import com.revakovskyi.giphy.core.network.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

interface NetworkManager {

    suspend fun loadGifsFromRemote(
        query: SearchQuery,
        offset: Int,
        amountToDownload: Int,
    ): Result<List<Gif>, DataError.Network>

}


internal class NetworkManagerImpl(
    connectivityObserver: ConnectivityObserver,
    private val apiService: ApiService,
) : NetworkManager {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val internetStatus: StateFlow<InternetStatus> = connectivityObserver.internetStatus
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = InternetStatus.Available
        )


    override suspend fun loadGifsFromRemote(
        query: SearchQuery,
        offset: Int,
        amountToDownload: Int,
    ): Result<List<Gif>, DataError.Network> {
        return if (internetStatus.value == InternetStatus.Available) {
            safeLoadGifs(
                query = query,
                offset = offset,
                amountToDownload = amountToDownload
            )
        } else Result.Error(DataError.Network.NO_INTERNET)
    }

    private suspend fun safeLoadGifs(
        query: SearchQuery,
        offset: Int,
        amountToDownload: Int,
    ): Result<List<Gif>, DataError.Network> {
        val result = safeCall {
            apiService.getGifsByQuery(
                query = query.query,
                offset = offset,
                limit = amountToDownload
            )
        }

        return when (result) {
            is Result.Error -> result
            is Result.Success -> Result.Success(result.data.data.map { it.toDomain(query.id) })
        }
    }

}
