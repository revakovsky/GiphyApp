package com.revakovskyi.giphy.app

import android.app.Application
import com.revakovskyi.giphy.app.di.appViewModelModule
import com.revakovskyi.giphy.core.data.di.dataCoreModule
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
                    appViewModelModule,
                    dataCoreModule,
                )
            )
        }
    }

}
