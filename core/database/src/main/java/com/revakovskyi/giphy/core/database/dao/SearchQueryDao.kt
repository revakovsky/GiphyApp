package com.revakovskyi.giphy.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.revakovskyi.giphy.core.database.entities.SearchQueryEntity

@Dao
interface SearchQueryDao {

    @Upsert
    suspend fun saveQuery(query: SearchQueryEntity)

    @Query("SELECT * FROM search_queries WHERE `query` = :query LIMIT 1")
    suspend fun getQuery(query: String): SearchQueryEntity?

    @Query("DELETE FROM search_queries WHERE `query` != :query")
    suspend fun clearOtherQueries(query: String)

}
