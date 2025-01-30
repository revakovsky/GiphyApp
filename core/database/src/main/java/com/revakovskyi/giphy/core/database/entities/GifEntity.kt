package com.revakovskyi.giphy.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "gifs",
    foreignKeys = [
        ForeignKey(
            entity = SearchQueryEntity::class,
            parentColumns = ["id"],
            childColumns = ["query_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["query_id"])]
)
data class GifEntity(
    @PrimaryKey @ColumnInfo(name = "gif_id") val gifId: String,
    @ColumnInfo(name = "query_id") val queryId: Long,
    @ColumnInfo(name = "url_small") val urlSmallImage: String,
    @ColumnInfo(name = "url_original") val urlOriginalImage: String,
)
