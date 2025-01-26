package com.revakovskyi.giphy.core.database.di

import androidx.room.Room
import com.revakovskyi.giphy.core.database.GifsDB
import com.revakovskyi.giphy.core.database.dao.DeletedGifsDao
import com.revakovskyi.giphy.core.database.dao.GifsDao
import com.revakovskyi.giphy.core.database.dao.SearchQueryDao
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {

    single<GifsDB> {
        Room.databaseBuilder(
            androidContext(),
            GifsDB::class.java,
            "GifsDB.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    single<GifsDao> { get<GifsDB>().gifsDao }
    single<SearchQueryDao> { get<GifsDB>().searchQueryDao }
    single<DeletedGifsDao> { get<GifsDB>().deletedGifsDao }

}
