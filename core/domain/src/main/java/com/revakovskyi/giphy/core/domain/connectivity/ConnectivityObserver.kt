package com.revakovskyi.giphy.core.domain.connectivity

import kotlinx.coroutines.flow.Flow

interface ConnectivityObserver {

    val internetStatus: Flow<InternetStatus>

}
