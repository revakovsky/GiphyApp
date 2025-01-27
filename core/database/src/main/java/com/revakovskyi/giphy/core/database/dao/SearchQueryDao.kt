package com.revakovskyi.giphy.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.revakovskyi.giphy.core.database.entities.SearchQueryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchQueryDao {

    @Upsert
    suspend fun saveQuery(query: SearchQueryEntity)

    @Query("SELECT * FROM search_queries LIMIT 1")
    fun getLastQuery(): Flow<SearchQueryEntity?>

    @Query("UPDATE search_queries SET current_page = :currentPage WHERE id = 1")
    suspend fun updateCurrentPage(currentPage: Int)

}
