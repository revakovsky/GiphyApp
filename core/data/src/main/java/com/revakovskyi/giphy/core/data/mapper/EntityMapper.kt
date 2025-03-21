package com.revakovskyi.giphy.core.data.mapper

import com.revakovskyi.giphy.core.database.entities.GifEntity
import com.revakovskyi.giphy.core.database.entities.SearchQueryEntity
import com.revakovskyi.giphy.core.domain.gifs.Gif
import com.revakovskyi.giphy.core.domain.gifs.SearchQuery

fun SearchQueryEntity.toDomain(): SearchQuery {
    return SearchQuery(
        id = id,
        query = query,
        currentPage = currentPage,
        deletedGifsAmount = deletedGifsAmount,
        maxGifPositionInTable = maxGifPositionInTable,
    )
}

fun SearchQuery.toEntity(): SearchQueryEntity {
    return SearchQueryEntity(
        query = query,
        currentPage = currentPage,
        timestamp = System.currentTimeMillis(),
        maxGifPositionInTable = maxGifPositionInTable,
        deletedGifsAmount = deletedGifsAmount,
    )
}

fun GifEntity.toDomain(): Gif {
    return Gif(
        id = gifId,
        queryId = queryId,
        urlSmallImage = urlSmallImage,
        urlOriginalImage = urlOriginalImage,
        position = position,
    )
}

fun Gif.toEntity(): GifEntity {
    return GifEntity(
        gifId = id,
        queryId = queryId,
        urlSmallImage = urlSmallImage,
        urlOriginalImage = urlOriginalImage,
        position = position
    )
}
