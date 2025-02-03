package com.revakovskyi.giphy.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.revakovskyi.giphy.core.database.dao.GifsDao
import com.revakovskyi.giphy.core.database.dao.SearchQueryDao
import com.revakovskyi.giphy.core.database.entities.GifEntity
import com.revakovskyi.giphy.core.database.entities.SearchQueryEntity

@Database(
    version = 1,
    entities = [
        GifEntity::class,
        SearchQueryEntity::class,
    ],
)
internal abstract class GifsDB : RoomDatabase() {

    abstract val gifsDao: GifsDao
    abstract val searchQueryDao: SearchQueryDao

}
