package com.flacify.music.core.update

import android.content.Context
import com.flacify.music.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GitHubUpdateManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val updateService: GitHubUpdateService
) {

    suspend fun checkForUpdates(
        owner: String = BuildConfig.GITHUB_OWNER,
        repo: String = BuildConfig.GITHUB_REPO
    ): UpdateCheckResult = withContext(Dispatchers.IO) {
        try {
            val release = updateService.getLatestRelease(owner, repo)
            val cleanTag = release.tagName.trim().removePrefix("v").removePrefix("V")
            val currentVersion = BuildConfig.VERSION_NAME.trim().removePrefix("v").removePrefix("V")

            if (isNewerVersion(cleanTag, currentVersion)) {
                val apkAsset = release.assets.firstOrNull {
                    it.name.endsWith(".apk", ignoreCase = true) && it.name.contains("release", ignoreCase = true)
                } ?: release.assets.firstOrNull {
                    it.name.endsWith(".apk", ignoreCase = true)
                }

                if (apkAsset != null) {
                    UpdateCheckResult.UpdateAvailable(
                        latestVersion = cleanTag,
                        releaseNotes = release.releaseNotes?.takeIf { it.isNotBlank() } ?: "No release notes provided.",
                        downloadUrl = apkAsset.downloadUrl,
                        apkSizeBytes = apkAsset.sizeBytes,
                        releasePageUrl = release.htmlUrl
                    )
                } else {
                    UpdateCheckResult.NoApkFound(
                        latestVersion = cleanTag,
                        releaseNotes = release.releaseNotes?.takeIf { it.isNotBlank() } ?: "No release notes provided.",
                        releasePageUrl = release.htmlUrl
                    )
                }
            } else {
                UpdateCheckResult.UpToDate
            }
        } catch (e: HttpException) {
            val message = when (e.code()) {
                404 -> "No release found on GitHub for repository $owner/$repo."
                403, 429 -> "GitHub API rate limit exceeded. Please try again later."
                else -> "GitHub API error (HTTP ${e.code()})"
            }
            UpdateCheckResult.Error(message = message, isNetworkError = false)
        } catch (e: IOException) {
            UpdateCheckResult.Error(
                message = "Unable to connect to GitHub. Please check your internet connection.",
                isNetworkError = true
            )
        } catch (e: Exception) {
            UpdateCheckResult.Error(
                message = e.localizedMessage ?: "Failed to check for updates",
                isNetworkError = false
            )
        }
    }

    fun isNewerVersion(remoteVersion: String, localVersion: String): Boolean {
        val cleanRemote = remoteVersion.trim().removePrefix("v").removePrefix("V")
        val cleanLocal = localVersion.trim().removePrefix("v").removePrefix("V")

        val remoteCore = cleanRemote.substringBefore("-").substringBefore("+")
        val localCore = cleanLocal.substringBefore("-").substringBefore("+")

        val remoteParts = remoteCore.split(".").mapNotNull { it.toIntOrNull() }
        val localParts = localCore.split(".").mapNotNull { it.toIntOrNull() }

        if (remoteParts.isEmpty() && localParts.isEmpty()) return false
        if (remoteParts.isEmpty()) return false
        if (localParts.isEmpty()) return true

        val maxLength = maxOf(remoteParts.size, localParts.size)
        for (i in 0 until maxLength) {
            val r = remoteParts.getOrElse(i) { 0 }
            val l = localParts.getOrElse(i) { 0 }
            if (r > l) return true
            if (r < l) return false
        }

        val remoteHasPre = cleanRemote.contains("-")
        val localHasPre = cleanLocal.contains("-")
        if (!remoteHasPre && localHasPre) {
            return true
        }

        return false
    }
}
