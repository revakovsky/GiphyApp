package com.revakovskyi.giphy.core.data.network

import com.revakovskyi.giphy.core.data.mapper.toDomain
import com.revakovskyi.giphy.core.domain.connectivity.ConnectivityObserver
import com.revakovskyi.giphy.core.domain.connectivity.InternetStatus
import com.revakovskyi.giphy.core.domain.gifs.models.Gif
import com.revakovskyi.giphy.core.domain.gifs.models.SearchQuery
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
    ): Result<List<Gif>, DataError.Network>

    suspend fun getGifById(gifId: String): Result<Gif, DataError.Network>

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
    ): Result<List<Gif>, DataError.Network> {
        return if (internetStatus.value == InternetStatus.Available) {
            val result = safeCall {
                apiService.getGifsByQuery(query = query.query, offset = offset)
            }

            when (result) {
                is Result.Error -> result

                is Result.Success -> {
                    Result.Success(
                        result.data.data.map { it.toDomain() }
                    )
                }
            }
        } else Result.Error(DataError.Network.NO_INTERNET)
    }

    override suspend fun getGifById(gifId: String): Result<Gif, DataError.Network> {
        return if (internetStatus.value == InternetStatus.Available) {
            val result = safeCall {
                apiService.getGifById(gifId = gifId)
            }

            when (result) {
                is Result.Error -> Result.Error(result.error)
                is Result.Success -> Result.Success(result.data.gifInfo.toDomain())
            }
        } else return Result.Error(DataError.Network.NO_INTERNET)
    }

}
