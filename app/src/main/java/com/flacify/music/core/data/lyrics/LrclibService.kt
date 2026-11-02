package com.flacify.music.core.data.lyrics

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface LrclibService {

    @GET("api/get")
    suspend fun getLyrics(
        @Query("track_name") trackName: String,
        @Query("artist_name") artistName: String,
        @Query("album_name") albumName: String? = null,
        @Query("duration") durationSeconds: Long? = null
    ): Response<LrclibResponse>

    @GET("api/search")
    suspend fun searchLyrics(
        @Query("q") query: String
    ): Response<List<LrclibResponse>>
}

