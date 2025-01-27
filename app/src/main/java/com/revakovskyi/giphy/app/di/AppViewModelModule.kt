package com.revakovskyi.giphy.app.di

import com.revakovskyi.giphy.app.presentation.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {

    viewModelOf(::MainViewModel)

}
