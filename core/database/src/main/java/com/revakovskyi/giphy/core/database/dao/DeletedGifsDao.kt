package com.revakovskyi.giphy.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.revakovskyi.giphy.core.database.entities.DeletedGifEntity

@Dao
interface DeletedGifsDao {

    @Upsert
    suspend fun insertDeletedGif(deletedGif: DeletedGifEntity)

    @Query("SELECT gif_id FROM deleted_gifs WHERE `query` = :query")
    suspend fun getDeletedGifIdsByQuery(query: String): List<String>

    @Query("DELETE FROM deleted_gifs")
    suspend fun clearDeletedGifs()

}
