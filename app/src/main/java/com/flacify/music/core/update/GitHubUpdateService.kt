package com.flacify.music.core.update

import retrofit2.http.GET
import retrofit2.http.Path

interface GitHubUpdateService {

    @GET("repos/{owner}/{repo}/releases/latest")
    suspend fun getLatestRelease(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): GitHubRelease
}

