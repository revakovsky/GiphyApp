package com.revakovskyi.giphy.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.revakovskyi.giphy.core.database.entities.GifEntity

@Dao
interface GifsDao {

    @Upsert
    suspend fun saveGifs(gifs: List<GifEntity>)

    @Query("SELECT * FROM gifs WHERE query_id = :queryId")
    suspend fun getGifsByQuery(queryId: Long): List<GifEntity>

    @Query("DELETE FROM gifs WHERE query_id = :queryId")
    suspend fun clearGifsByQuery(queryId: Long)

}
