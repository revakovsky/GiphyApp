package com.revakovskyi.giphy.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.revakovskyi.giphy.core.database.entities.GifEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GifsDao {

    @Query("SELECT NOT EXISTS (SELECT 1 FROM gifs LIMIT 1)")
    fun isDbEmpty(): Flow<Boolean>

    @Upsert
    suspend fun saveGifs(gifs: List<GifEntity>)

    @Query("SELECT * FROM gifs WHERE query_id = :queryId ORDER BY position ASC LIMIT :limit OFFSET :offset")
    suspend fun getGifsByQuery(queryId: Long, limit: Int, offset: Int): List<GifEntity>

    @Query("SELECT * FROM gifs WHERE query_id = :queryId")
    fun getGifsByQueryId(queryId: Long): Flow<List<GifEntity>>

    @Query("SELECT MAX(position) FROM gifs WHERE query_id = :queryId")
    suspend fun getMaxPosition(queryId: Long): Int?

    @Query("DELETE FROM gifs WHERE gif_id = :gifId")
    suspend fun deleteGif(gifId: String)

}
