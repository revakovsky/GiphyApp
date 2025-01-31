package com.revakovskyi.giphy.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "search_queries",
    indices = [Index(value = ["query"], unique = true)]
)
data class SearchQueryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "query") val query: String,
    @ColumnInfo(name = "current_page") val currentPage: Int,
    @ColumnInfo(name = "successful") val wasSuccessful: Boolean = false,
    @ColumnInfo(name = "timestamp") val timestamp: Long = System.currentTimeMillis(),
)
