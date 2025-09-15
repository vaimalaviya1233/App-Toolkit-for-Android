package com.d4rk.android.apps.apptoolkit.core.broadcast

import android.content.Context
import android.content.Intent
import android.util.Log
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class FavoritesChangedReceiverTest {

    private val context: Context = mockk(relaxed = true)
    private lateinit var receiver: FavoritesChangedReceiver

    @BeforeEach
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        receiver = FavoritesChangedReceiver()
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `onReceive logs the provided package name`() {
        val intent = Intent(FavoritesChangedReceiver.ACTION_FAVORITES_CHANGED).apply {
            putExtra(FavoritesChangedReceiver.EXTRA_PACKAGE_NAME, "com.example.app")
        }

        receiver.onReceive(context, intent)

        verify(exactly = 1) {
            Log.d("FavoritesChangedRcvr", "Favorites changed: com.example.app")
        }
    }

    @Test
    fun `onReceive handles null intent without crashing`() {
        assertDoesNotThrow {
            receiver.onReceive(context, null)
        }

        verify(exactly = 1) {
            Log.d("FavoritesChangedRcvr", "Favorites changed: null")
        }
    }

    @Test
    fun `onReceive handles missing package extra without crashing`() {
        val intent = Intent(FavoritesChangedReceiver.ACTION_FAVORITES_CHANGED)

        assertDoesNotThrow {
            receiver.onReceive(context, intent)
        }

        verify(exactly = 1) {
            Log.d("FavoritesChangedRcvr", "Favorites changed: null")
        }
    }
}
