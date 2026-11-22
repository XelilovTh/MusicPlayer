package com.tehmaz.musicplayer

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.tehmaz.musicplayer.data.scanner.AudioScanner
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
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
        mockkStatic(ContextCompat::class)
        context = mockk(relaxed = true)
        scanner = AudioScanner(context)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun hasPermission_whenPermissionDenied_returnsFalse() {
        every { ContextCompat.checkSelfPermission(any(), any()) } returns PackageManager.PERMISSION_DENIED
        every { context.checkPermission(any(), any(), any()) } returns PackageManager.PERMISSION_DENIED

        assertFalse(scanner.hasPermission())
    }

    @Test
    fun hasPermission_whenPermissionGranted_returnsTrue() {
        every { ContextCompat.checkSelfPermission(any(), any()) } returns PackageManager.PERMISSION_GRANTED
        every { context.checkPermission(any(), any(), any()) } returns PackageManager.PERMISSION_GRANTED

        assertTrue(scanner.hasPermission())
    }

    @Test
    fun scanLocalSongs_whenPermissionMissing_returnsEmptyList() = runTest {
        every { ContextCompat.checkSelfPermission(any(), any()) } returns PackageManager.PERMISSION_DENIED
        every { context.checkPermission(any(), any(), any()) } returns PackageManager.PERMISSION_DENIED

        val songs = scanner.scanLocalSongs()
        assertTrue(songs.isEmpty())
        assertEquals(0, songs.size)
    }
}
