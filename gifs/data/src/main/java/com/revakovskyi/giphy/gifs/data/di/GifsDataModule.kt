package com.revakovskyi.giphy.gifs.data.di

import com.revakovskyi.giphy.gifs.data.GifsRepositoryImpl
import com.revakovskyi.giphy.gifs.domain.GifsRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val gifsDataModule = module {

    singleOf(::GifsRepositoryImpl).bind<GifsRepository>()

}
