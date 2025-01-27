package com.revakovskyi.giphy.app

import android.app.Application
import com.revakovskyi.giphy.app.di.appModule
import com.revakovskyi.giphy.core.data.di.dataCoreModule
import com.revakovskyi.giphy.core.database.di.databaseModule
import com.revakovskyi.giphy.core.network.di.networkModule
import com.revakovskyi.giphy.gifs.data.di.gifsDataModule
import com.revakovskyi.giphy.gifs.presentation.di.gifsPresentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        setUpKoin()
    }

    private fun setUpKoin() {
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@App)
            modules(
                listOf(
                    appModule,
                    dataCoreModule,
                    networkModule,
                    databaseModule,
                    gifsPresentationModule,
                    gifsDataModule,
                )
            )
        }
    }

}
