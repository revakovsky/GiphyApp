package com.revakovskyi.giphy.gifs.presentation.di

import com.revakovskyi.giphy.gifs.presentation.gifs.GifsViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val gifsPresentationModule = module {

    viewModelOf(::GifsViewModel)

}
