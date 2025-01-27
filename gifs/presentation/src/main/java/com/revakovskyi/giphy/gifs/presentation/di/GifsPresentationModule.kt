package com.revakovskyi.giphy.gifs.presentation.di

import com.revakovskyi.giphy.core.presentation.ui.uitls.QueryValidator
import com.revakovskyi.giphy.core.presentation.ui.uitls.QueryValidatorImpl
import com.revakovskyi.giphy.gifs.presentation.gifs.GifsViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val gifsPresentationModule = module {

    singleOf(::QueryValidatorImpl).bind<QueryValidator>()

    viewModelOf(::GifsViewModel)

}
