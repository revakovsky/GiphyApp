package com.revakovskyi.giphy.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.revakovskyi.giphy.core.database.entities.SearchQueryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchQueryDao {

    @Query("DELETE FROM search_queries WHERE successful = 0")
    suspend fun clearUnsuccessfulSearchQueries()

    @Query("UPDATE search_queries SET successful = 1 WHERE id = :queryId")
    suspend fun markQueryAsSuccessful(queryId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun saveQuery(query: SearchQueryEntity)

    @Query("SELECT * FROM search_queries ORDER BY id DESC LIMIT 1")
    fun getLastQuery(): Flow<SearchQueryEntity?>

    @Query("UPDATE search_queries SET current_page = :currentPage WHERE id = :id")
    suspend fun saveCurrentPage(id: Long, currentPage: Int)

    @Query("SELECT * FROM search_queries WHERE `query` = :queryText LIMIT 1")
    suspend fun getQueryByText(queryText: String): SearchQueryEntity?

    @Query(
        """
            DELETE FROM search_queries
            WHERE id NOT IN (
            SELECT id FROM search_queries
            ORDER BY id DESC
            LIMIT :limit
            )
        """
    )
    suspend fun deleteOldQueries(limit: Int = 100)

}
