package com.revakovskyi.giphy.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deleted_gifs")
data class DeletedGifEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "gif_id") val gifId: String,
    @ColumnInfo(name = "query") val query: String,
)
