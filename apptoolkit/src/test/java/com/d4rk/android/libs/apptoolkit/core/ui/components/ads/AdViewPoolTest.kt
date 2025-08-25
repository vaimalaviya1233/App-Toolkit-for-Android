package com.d4rk.android.libs.apptoolkit.core.ui.components.ads

import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.d4rk.android.libs.apptoolkit.core.utils.dispatchers.UnconfinedDispatcherExtension
import io.mockk.Runs
import io.mockk.anyConstructed
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkConstructor
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.assertDoesNotThrow

@ExtendWith(UnconfinedDispatcherExtension::class)
class AdViewPoolTest {

    @AfterEach
    fun teardown() {
        unmockkConstructor(AdView::class)
        val field = AdViewPool::class.java.getDeclaredField("pool")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        (field.get(AdViewPool) as MutableMap<*, *>).clear()
    }

    @Test
    fun `preload does not throw when exceeding pool size`() = runBlocking {
        mockkConstructor(AdView::class)
        val context = mockk<Context>(relaxed = true)
        every { context.applicationContext } returns context
        every { anyConstructed<AdView>().adUnitId = any() } just Runs
        every { anyConstructed<AdView>().setAdSize(any()) } just Runs
        every { anyConstructed<AdView>().loadAd(any()) } just Runs
        every { anyConstructed<AdView>().destroy() } just Runs
        every { anyConstructed<AdView>().post(any()) } answers {
            firstArg<Runnable>().run()
            true
        }
        val adRequest = mockk<AdRequest>()

        assertDoesNotThrow {
            AdViewPool.preload(context, "unit", AdSize.BANNER, adRequest, 4)
        }
        delay(50)
    }
}

