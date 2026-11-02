package com.flacify.music.core.update

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubRelease(
    @SerialName("tag_name")
    val tagName: String,
    @SerialName("name")
    val name: String? = null,
    @SerialName("body")
    val releaseNotes: String? = null,
    @SerialName("assets")
    val assets: List<GitHubAsset> = emptyList()
)

@Serializable
data class GitHubAsset(
    @SerialName("name")
    val name: String,
    @SerialName("size")
    val sizeBytes: Long,
    @SerialName("browser_download_url")
    val downloadUrl: String
)

sealed interface UpdateCheckResult {
    data object UpToDate : UpdateCheckResult
    data class UpdateAvailable(
        val latestVersion: String,
        val releaseNotes: String,
        val downloadUrl: String,
        val apkSizeBytes: Long
    ) : UpdateCheckResult
    data class Error(val message: String) : UpdateCheckResult
}

