package com.revakovskyi.giphy.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.revakovskyi.giphy.core.database.entities.SearchQueryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchQueryDao {

    @Query("DELETE FROM search_queries WHERE successful = 0")
    suspend fun clearUnsuccessfulSearchQueries()

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

    @Query("UPDATE search_queries SET successful = 1 WHERE id = :queryId")
    suspend fun markQueryAsSuccessful(queryId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun saveQuery(query: SearchQueryEntity)

    @Update
    suspend fun updateQuery(query: SearchQueryEntity)

    @Query("SELECT * FROM search_queries ORDER BY timestamp DESC LIMIT 1")
    fun getLastQuery(): Flow<SearchQueryEntity?>

    @Query("SELECT * FROM search_queries WHERE `query` = :queryText LIMIT 1")
    suspend fun getSearchQueryByQueryText(queryText: String): SearchQueryEntity?

    @Query("UPDATE search_queries SET current_page = :currentPage, timestamp = :timestamp WHERE id = :id")
    suspend fun updateCurrentPage(
        id: Long,
        currentPage: Int,
        timestamp: Long = System.currentTimeMillis(),
    )

    @Query("UPDATE search_queries SET max_position = :maxPosition WHERE id = :queryId")
    suspend fun updateGifsMaxPosition(queryId: Long, maxPosition: Int)

    @Query("UPDATE search_queries SET deleted_gifs_amount = :deletedGifsAmount WHERE id = :queryId")
    suspend fun updateDeletedGifsAmount(queryId: Long, deletedGifsAmount: Int)

}
