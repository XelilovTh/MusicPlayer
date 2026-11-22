package com.tehmaz.musicplayer

import android.content.Context
import android.content.pm.PackageManager
import com.tehmaz.musicplayer.data.scanner.AudioScanner
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AudioScannerTest {

    private lateinit var context: Context
    private lateinit var scanner: AudioScanner

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        scanner = AudioScanner(context)
    }

    @Test
    fun hasPermission_whenPermissionDenied_returnsFalse() {
        every { context.checkPermission(any(), any(), any()) } returns PackageManager.PERMISSION_DENIED

        assertFalse(scanner.hasPermission())
    }

    @Test
    fun scanLocalSongs_whenPermissionMissing_returnsEmptyList() = runTest {
        every { context.checkPermission(any(), any(), any()) } returns PackageManager.PERMISSION_DENIED

        val songs = scanner.scanLocalSongs()
        assertTrue(songs.isEmpty())
        assertEquals(0, songs.size)
    }
}
