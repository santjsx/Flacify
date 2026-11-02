package com.flacify.music

import com.flacify.music.core.update.GitHubAsset
import com.flacify.music.core.update.GitHubRelease
import com.flacify.music.core.update.GitHubUpdateManager
import com.flacify.music.core.update.GitHubUpdateService
import com.flacify.music.core.update.UpdateCheckResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class GitHubUpdateManagerTest {

    private val updateService: GitHubUpdateService = mockk(relaxed = true)
    private val updateManager = GitHubUpdateManager(mockk(relaxed = true), updateService)

    // --- SemVer Unit Tests ---

    @Test
    fun isNewerVersion_newerMajorVersion_returnsTrue() {
        assertTrue(updateManager.isNewerVersion("2.0.0", "1.0.0"))
    }

    @Test
    fun isNewerVersion_newerMinorVersion_returnsTrue() {
        assertTrue(updateManager.isNewerVersion("1.1.0", "1.0.0"))
    }

    @Test
    fun isNewerVersion_newerPatchVersion_returnsTrue() {
        assertTrue(updateManager.isNewerVersion("1.0.1", "1.0.0"))
    }

    @Test
    fun isNewerVersion_withVPrefix_returnsTrue() {
        assertTrue(updateManager.isNewerVersion("v1.0.1", "1.0.0"))
        assertTrue(updateManager.isNewerVersion("V1.0.1", "v1.0.0"))
    }

    @Test
    fun isNewerVersion_withBuildMetadata_returnsTrue() {
        assertTrue(updateManager.isNewerVersion("1.0.1+build.42", "1.0.0"))
    }

    @Test
    fun isNewerVersion_stableOverPrerelease_returnsTrue() {
        assertTrue(updateManager.isNewerVersion("1.0.0", "1.0.0-rc1"))
        assertTrue(updateManager.isNewerVersion("1.0.0", "1.0.0-beta"))
    }

    @Test
    fun isNewerVersion_prereleaseUnderStable_returnsFalse() {
        assertFalse(updateManager.isNewerVersion("1.0.0-rc1", "1.0.0"))
    }

    @Test
    fun isNewerVersion_differingPartLengths_handlesCorrectly() {
        assertFalse(updateManager.isNewerVersion("1.0", "1.0.0"))
        assertTrue(updateManager.isNewerVersion("1.1", "1.0.0"))
        assertFalse(updateManager.isNewerVersion("1.0.0", "1.0"))
        assertTrue(updateManager.isNewerVersion("1.0.1", "1.0"))
    }

    @Test
    fun isNewerVersion_identicalVersion_returnsFalse() {
        assertFalse(updateManager.isNewerVersion("1.0.0", "1.0.0"))
        assertFalse(updateManager.isNewerVersion("v1.0.0", "1.0.0"))
    }

    @Test
    fun isNewerVersion_olderVersion_returnsFalse() {
        assertFalse(updateManager.isNewerVersion("0.9.5", "1.0.0"))
    }

    @Test
    fun isNewerVersion_malformedVersion_returnsFalseSafely() {
        assertFalse(updateManager.isNewerVersion("invalid", "1.0.0"))
        assertFalse(updateManager.isNewerVersion("", "1.0.0"))
    }

    // --- checkForUpdates Flow Tests ---

    @Test
    fun checkForUpdates_availableReleaseWithApk_returnsUpdateAvailable() = runTest {
        coEvery { updateService.getLatestRelease(any(), any()) } returns GitHubRelease(
            tagName = "v2.0.0",
            name = "FLACify v2.0.0",
            releaseNotes = "Big update",
            htmlUrl = "https://github.com/santjsx/Flacify/releases/tag/v2.0.0",
            assets = listOf(
                GitHubAsset(
                    name = "app-release.apk",
                    sizeBytes = 7_000_000L,
                    downloadUrl = "https://github.com/santjsx/Flacify/releases/download/v2.0.0/app-release.apk"
                )
            )
        )

        val result = updateManager.checkForUpdates(owner = "santjsx", repo = "Flacify")
        assertTrue(result is UpdateCheckResult.UpdateAvailable)
        val available = result as UpdateCheckResult.UpdateAvailable
        assertEquals("2.0.0", available.latestVersion)
        assertEquals("Big update", available.releaseNotes)
        assertEquals(7_000_000L, available.apkSizeBytes)
    }

    @Test
    fun checkForUpdates_newerReleaseWithoutApk_returnsNoApkFound() = runTest {
        coEvery { updateService.getLatestRelease(any(), any()) } returns GitHubRelease(
            tagName = "v2.0.0",
            name = "FLACify v2.0.0 Source Only",
            releaseNotes = "Source release",
            htmlUrl = "https://github.com/santjsx/Flacify/releases/tag/v2.0.0",
            assets = emptyList()
        )

        val result = updateManager.checkForUpdates(owner = "santjsx", repo = "Flacify")
        assertTrue(result is UpdateCheckResult.NoApkFound)
        val noApk = result as UpdateCheckResult.NoApkFound
        assertEquals("2.0.0", noApk.latestVersion)
        assertEquals("https://github.com/santjsx/Flacify/releases/tag/v2.0.0", noApk.releasePageUrl)
    }

    @Test
    fun checkForUpdates_sameVersion_returnsUpToDate() = runTest {
        coEvery { updateService.getLatestRelease(any(), any()) } returns GitHubRelease(
            tagName = "v1.0.0",
            name = "FLACify v1.0.0",
            assets = listOf(
                GitHubAsset(
                    name = "app-release.apk",
                    sizeBytes = 7_000_000L,
                    downloadUrl = "https://github.com/santjsx/Flacify/releases/download/v1.0.0/app-release.apk"
                )
            )
        )

        val result = updateManager.checkForUpdates(owner = "santjsx", repo = "Flacify")
        assertTrue(result is UpdateCheckResult.UpToDate)
    }

    @Test
    fun checkForUpdates_http404_returnsClearRepositoryError() = runTest {
        val errorResponse = Response.error<GitHubRelease>(
            404,
            "{\"message\":\"Not Found\"}".toResponseBody("application/json".toMediaType())
        )
        coEvery { updateService.getLatestRelease(any(), any()) } throws HttpException(errorResponse)

        val result = updateManager.checkForUpdates(owner = "santjsx", repo = "Flacify")
        assertTrue(result is UpdateCheckResult.Error)
        val error = result as UpdateCheckResult.Error
        assertFalse(error.isNetworkError)
        assertTrue(error.message.contains("No release found on GitHub"))
    }

    @Test
    fun checkForUpdates_networkFailure_returnsNetworkError() = runTest {
        coEvery { updateService.getLatestRelease(any(), any()) } throws IOException("Failed to connect")

        val result = updateManager.checkForUpdates(owner = "santjsx", repo = "Flacify")
        assertTrue(result is UpdateCheckResult.Error)
        val error = result as UpdateCheckResult.Error
        assertTrue(error.isNetworkError)
        assertTrue(error.message.contains("check your internet connection"))
    }
}
