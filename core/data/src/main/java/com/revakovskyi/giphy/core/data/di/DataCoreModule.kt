package com.revakovskyi.giphy.core.data.di

import com.revakovskyi.giphy.core.data.connectivity.InternetConnectivityObserver
import com.revakovskyi.giphy.core.data.local_db.DbManager
import com.revakovskyi.giphy.core.data.local_db.LocalDbManager
import com.revakovskyi.giphy.core.data.network.NetworkManager
import com.revakovskyi.giphy.core.data.network.NetworkManagerImpl
import com.revakovskyi.giphy.core.domain.connectivity.ConnectivityObserver
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataCoreModule = module {

    singleOf(::InternetConnectivityObserver).bind<ConnectivityObserver>()
    singleOf(::LocalDbManager).bind<DbManager>()
    singleOf(::NetworkManagerImpl).bind<NetworkManager>()

}
