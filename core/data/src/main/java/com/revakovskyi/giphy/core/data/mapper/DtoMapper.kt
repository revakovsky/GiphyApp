package com.revakovskyi.giphy.core.data.mapper

import com.revakovskyi.giphy.core.domain.gifs.Gif
import com.revakovskyi.giphy.core.network.dto.GifInfo

fun GifInfo.toDomain(queryId: Long, index: Int): Gif {
    return Gif(
        id = id,
        queryId = queryId,
        urlSmallImage = images.small.url,
        urlOriginalImage = images.original.url,
        position = index + 1,
    )
}
