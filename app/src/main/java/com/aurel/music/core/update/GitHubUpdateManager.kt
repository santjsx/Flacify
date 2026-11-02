package com.aurel.music.core.update

import android.content.Context
import com.aurel.music.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GitHubUpdateManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val updateService: GitHubUpdateService
) {

    suspend fun checkForUpdates(owner: String = "aurel-music", repo: String = "aurel"): UpdateCheckResult =
        withContext(Dispatchers.IO) {
            try {
                val release = updateService.getLatestRelease(owner, repo)
                val cleanTag = release.tagName.removePrefix("v").trim()
                val currentVersion = BuildConfig.VERSION_NAME.removePrefix("v").trim()

                if (isNewerVersion(cleanTag, currentVersion)) {
                    val apkAsset = release.assets.find { it.name.endsWith(".apk") }
                    if (apkAsset != null) {
                        UpdateCheckResult.UpdateAvailable(
                            latestVersion = cleanTag,
                            releaseNotes = release.releaseNotes ?: "No release notes provided.",
                            downloadUrl = apkAsset.downloadUrl,
                            apkSizeBytes = apkAsset.sizeBytes
                        )
                    } else {
                        UpdateCheckResult.UpToDate
                    }
                } else {
                    UpdateCheckResult.UpToDate
                }
            } catch (e: Exception) {
                UpdateCheckResult.Error(e.message ?: "Failed to check for updates")
            }
        }

    fun isNewerVersion(remoteVersion: String, localVersion: String): Boolean {
        val remoteParts = remoteVersion.split(".").mapNotNull { it.toIntOrNull() }
        val localParts = localVersion.split(".").mapNotNull { it.toIntOrNull() }

        val length = maxOf(remoteParts.size, localParts.size)
        for (i in 0 until length) {
            val r = remoteParts.getOrElse(i) { 0 }
            val l = localParts.getOrElse(i) { 0 }
            if (r > l) return true
            if (r < l) return false
        }
        return false
    }
}
