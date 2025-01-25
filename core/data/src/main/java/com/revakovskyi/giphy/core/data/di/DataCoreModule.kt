package com.revakovskyi.giphy.core.data.di

import com.revakovskyi.giphy.core.data.connectivity.InternetConnectivityObserver
import com.revakovskyi.giphy.core.domain.connectivity.ConnectivityObserver
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataCoreModule = module {

    singleOf(::InternetConnectivityObserver).bind<ConnectivityObserver>()

}
