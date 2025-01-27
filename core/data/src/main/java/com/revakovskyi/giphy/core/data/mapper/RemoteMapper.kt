package com.revakovskyi.giphy.core.data.mapper

import com.revakovskyi.giphy.core.domain.gifs.models.Gif
import com.revakovskyi.giphy.core.network.dto.gif_by_id.GifInfo
import com.revakovskyi.giphy.core.network.dto.searched_gifs.SearchedGifsInfo

fun SearchedGifsInfo.toDomain(): Gif {
    return Gif(
        id = id,
        url = images.fixedWidthSmall.url
    )
}

fun GifInfo.toDomain(): Gif {
    return Gif(
        id = id,
        url = images.original.url
    )
}
