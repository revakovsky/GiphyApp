package com.revakovskyi.giphy.core.network

import com.revakovskyi.giphy.core.network.dto.gif_by_id.IdGifDto
import com.revakovskyi.giphy.core.network.dto.searched_gifs.SearchedGifsDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("search")
    suspend fun getGifsByQuery(
        @Query("q") query: String,
        @Query("offset") offset: Int,
        @Query("api_key") apiKey: String = BuildConfig.API_KEY,
        @Query("limit") limit: Int = LIMIT,
        @Query("rating") rating: String = RATING,
        @Query("lang") lang: String = LANGUAGE,
        @Query("bundle") bundle: String = BUNDLE,
    ): Response<SearchedGifsDto>

    @GET("{id}")
    suspend fun getGifById(
        @Path("id") gifId: String,
        @Query("api_key") apiKey: String = BuildConfig.API_KEY,
        @Query("rating") rating: String = RATING,
    ): Response<IdGifDto>


    companion object {
        const val LIMIT = 25
        private const val RATING = "g"
        private const val LANGUAGE = "en"
        private const val BUNDLE = "messaging_non_clips"
    }

}