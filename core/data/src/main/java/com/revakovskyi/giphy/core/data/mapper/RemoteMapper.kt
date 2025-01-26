package com.revakovskyi.giphy.core.data.mapper

import com.revakovskyi.giphy.core.database.entities.GifEntity
import com.revakovskyi.giphy.core.network.dto.searched_gifs.Data

fun Data.toEntity(queryId: Long): GifEntity {
    return GifEntity(
        gifId = id,
        queryId = queryId,
        url = images.fixedWidthSmall.url
    )
}
