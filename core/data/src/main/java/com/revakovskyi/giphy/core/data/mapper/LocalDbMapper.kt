package com.revakovskyi.giphy.core.data.mapper

import com.revakovskyi.giphy.core.database.entities.DeletedGifEntity
import com.revakovskyi.giphy.core.database.entities.GifEntity
import com.revakovskyi.giphy.core.database.entities.SearchQueryEntity
import com.revakovskyi.giphy.core.domain.gifs.models.DeletedGif
import com.revakovskyi.giphy.core.domain.gifs.models.Gif
import com.revakovskyi.giphy.core.domain.gifs.models.SearchQuery

fun SearchQueryEntity.toDomain(): SearchQuery {
    return SearchQuery(
        id = id,
        query = query,
        currentPage = currentPage
    )
}

fun SearchQuery.toEntity(): SearchQueryEntity {
    return SearchQueryEntity(
        id = id,
        query = query,
        currentPage = currentPage
    )
}

fun DeletedGifEntity.toDomain(): DeletedGif {
    return DeletedGif(
        gifId = gifId,
        query = query
    )
}

fun DeletedGif.toEntity(): DeletedGifEntity {
    return DeletedGifEntity(
        gifId = gifId,
        query = query
    )
}

fun GifEntity.toDomain(): Gif {
    return Gif(
        id = gifId,
        url = url
    )
}
