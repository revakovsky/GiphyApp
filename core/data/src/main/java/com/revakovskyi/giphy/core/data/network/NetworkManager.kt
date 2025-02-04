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

    suspend fun fetchGifsFromApi(
        query: SearchQuery,
        offset: Int,
        limit: Int,
        onSuccess: suspend (success: Result.Success<List<Gif>>) -> Unit,
        onError: suspend (error: Result.Error<DataError.Network>) -> Unit,
    )

}


internal class NetworkManagerImpl(
    connectivityObserver: ConnectivityObserver,
    private val apiService: ApiService,
) : NetworkManager {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val internetStatus: StateFlow<InternetStatus> = connectivityObserver.internetStatus
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = InternetStatus.Available
        )


    override suspend fun fetchGifsFromApi(
        query: SearchQuery,
        offset: Int,
        limit: Int,
        onSuccess: suspend (success: Result.Success<List<Gif>>) -> Unit,
        onError: suspend (error: Result.Error<DataError.Network>) -> Unit,
    ) {
        if (internetStatus.value == InternetStatus.Available) {
            val result = safeCall {
                apiService.getGifsByQuery(query = query.query, offset = offset, limit = limit)
            }

            when (result) {
                is Result.Error -> onError(result)
                is Result.Success -> {
                    onSuccess(
                        Result.Success(
                            result.data.data.mapIndexed { index, gifDto ->
                                gifDto.toDomain(query.id, index)
                            }
                        )
                    )
                }
            }
        } else onError(Result.Error(DataError.Network.NO_INTERNET))
    }

}
