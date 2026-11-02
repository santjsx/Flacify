package com.aurel.music

import com.aurel.music.core.update.GitHubUpdateManager
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GitHubUpdateManagerTest {

    private val updateManager = GitHubUpdateManager(mockk(relaxed = true), mockk(relaxed = true))

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
    fun isNewerVersion_identicalVersion_returnsFalse() {
        assertFalse(updateManager.isNewerVersion("1.0.0", "1.0.0"))
    }

    @Test
    fun isNewerVersion_olderVersion_returnsFalse() {
        assertFalse(updateManager.isNewerVersion("0.9.5", "1.0.0"))
    }
}
