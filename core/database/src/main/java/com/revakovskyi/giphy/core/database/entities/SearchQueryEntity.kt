package com.revakovskyi.giphy.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_queries")
data class SearchQueryEntity(
    @PrimaryKey val id: Long = 1,
    @ColumnInfo(name = "query") val query: String,
    @ColumnInfo(name = "current_page") val currentPage: Int,
)
