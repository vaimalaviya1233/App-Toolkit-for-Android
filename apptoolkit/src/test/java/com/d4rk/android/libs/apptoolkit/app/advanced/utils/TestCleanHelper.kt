package com.d4rk.android.libs.apptoolkit.app.advanced.utils

import android.content.Context
import android.widget.Toast
import com.d4rk.android.libs.apptoolkit.R
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Test
import kotlin.io.path.createTempDirectory
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest

class TestCleanHelper {

    @Test
    fun `clearApplicationCache deletes cache directories`() = runTest {
        println("🚀 [TEST] clearApplicationCache deletes cache directories")
        val dir1 = createTempDirectory().toFile()
        val dir2 = createTempDirectory().toFile()
        val dir3 = createTempDirectory().toFile()

        val context = mockk<Context>()
        every { context.cacheDir } returns dir1
        every { context.codeCacheDir } returns dir2
        every { context.filesDir } returns dir3
        every { context.getString(R.string.cache_cleared_success) } returns "success"

        mockkStatic(Toast::class)
        val toast = mockk<Toast>(relaxed = true)
        every { Toast.makeText(context, "success", Toast.LENGTH_SHORT) } returns toast

        CleanHelper.clearApplicationCache(context)

        assertFalse(dir1.exists())
        assertFalse(dir2.exists())
        assertFalse(dir3.exists())
        verify { context.getString(R.string.cache_cleared_success) }
        verify { Toast.makeText(context, "success", Toast.LENGTH_SHORT) }
        println("🏁 [TEST DONE] clearApplicationCache deletes cache directories")
    }

    @Test
    fun `clearApplicationCache shows error toast when deletion fails`() = runTest {
        println("🚀 [TEST] clearApplicationCache shows error toast when deletion fails")
        val dir1 = createTempDirectory().toFile()
        val failing = mockk<java.io.File>()
        every { failing.deleteRecursively() } returns false
        every { failing.exists() } returns true
        val dir3 = createTempDirectory().toFile()

        val context = mockk<Context>()
        every { context.cacheDir } returns dir1
        every { context.codeCacheDir } returns failing
        every { context.filesDir } returns dir3
        every { context.getString(R.string.cache_cleared_error) } returns "error"

        mockkStatic(Toast::class)
        val toast = mockk<Toast>(relaxed = true)
        every { Toast.makeText(context, "error", Toast.LENGTH_SHORT) } returns toast

        CleanHelper.clearApplicationCache(context)

        assertFalse(dir1.exists())
        assertFalse(dir3.exists())
        verify { failing.deleteRecursively() }
        verify { context.getString(R.string.cache_cleared_error) }
        verify { Toast.makeText(context, "error", Toast.LENGTH_SHORT) }
        println("🏁 [TEST DONE] clearApplicationCache shows error toast when deletion fails")
    }

    @Test
    fun `clearApplicationCache throws when directory inaccessible`() = runTest {
        println("🚀 [TEST] clearApplicationCache throws when directory inaccessible")
        val context = mockk<Context>()
        every { context.cacheDir } throws SecurityException("denied")

        assertFailsWith<SecurityException> {
            CleanHelper.clearApplicationCache(context)
        }
        println("🏁 [TEST DONE] clearApplicationCache throws when directory inaccessible")
    }

    @Test
    fun `clearApplicationCache handles missing directories`() = runTest {
        println("🚀 [TEST] clearApplicationCache handles missing directories")
        val dir1 = createTempDirectory().toFile().also { it.deleteRecursively() }
        val dir2 = createTempDirectory().toFile().also { it.deleteRecursively() }
        val dir3 = createTempDirectory().toFile().also { it.deleteRecursively() }

        val context = mockk<Context>()
        every { context.cacheDir } returns dir1
        every { context.codeCacheDir } returns dir2
        every { context.filesDir } returns dir3
        every { context.getString(R.string.cache_cleared_success) } returns "success"

        mockkStatic(Toast::class)
        val toast = mockk<Toast>(relaxed = true)
        every { Toast.makeText(context, "success", Toast.LENGTH_SHORT) } returns toast

        CleanHelper.clearApplicationCache(context)

        verify { context.getString(R.string.cache_cleared_success) }
        verify { Toast.makeText(context, "success", Toast.LENGTH_SHORT) }
        println("🏁 [TEST DONE] clearApplicationCache handles missing directories")
    }

    @Test
    fun `clearApplicationCache propagates io exception`() = runTest {
        println("🚀 [TEST] clearApplicationCache propagates io exception")
        val dir1 = createTempDirectory().toFile()
        val failing = mockk<java.io.File>()
        every { failing.deleteRecursively() } throws java.io.IOException("io")
        every { failing.exists() } returns true
        val dir3 = createTempDirectory().toFile()

        val context = mockk<Context>()
        every { context.cacheDir } returns dir1
        every { context.codeCacheDir } returns failing
        every { context.filesDir } returns dir3

        assertFailsWith<java.io.IOException> {
            CleanHelper.clearApplicationCache(context)
        }
        println("🏁 [TEST DONE] clearApplicationCache propagates io exception")
    }

    @Test
    fun `clearApplicationCache propagates toast exception`() = runTest {
        println("🚀 [TEST] clearApplicationCache propagates toast exception")
        val dir1 = createTempDirectory().toFile()
        val dir2 = createTempDirectory().toFile()
        val dir3 = createTempDirectory().toFile()

        val context = mockk<Context>()
        every { context.cacheDir } returns dir1
        every { context.codeCacheDir } returns dir2
        every { context.filesDir } returns dir3
        every { context.getString(R.string.cache_cleared_success) } returns "success"

        mockkStatic(Toast::class)
        every { Toast.makeText(context, "success", Toast.LENGTH_SHORT) } throws RuntimeException("toast")

        assertFailsWith<RuntimeException> {
            CleanHelper.clearApplicationCache(context)
        }
        println("🏁 [TEST DONE] clearApplicationCache propagates toast exception")
    }

    @Test
    fun `clearApplicationCache handles partial deletion`() = runTest {
        println("🚀 [TEST] clearApplicationCache handles partial deletion")
        val dir1 = createTempDirectory().toFile()
        val failing2 = mockk<java.io.File>()
        every { failing2.deleteRecursively() } returns false
        every { failing2.exists() } returns true
        val failing3 = mockk<java.io.File>()
        every { failing3.deleteRecursively() } returns false
        every { failing3.exists() } returns true

        val context = mockk<Context>()
        every { context.cacheDir } returns dir1
        every { context.codeCacheDir } returns failing2
        every { context.filesDir } returns failing3
        every { context.getString(R.string.cache_cleared_error) } returns "error"

        mockkStatic(Toast::class)
        val toast = mockk<Toast>(relaxed = true)
        every { Toast.makeText(context, "error", Toast.LENGTH_SHORT) } returns toast

        CleanHelper.clearApplicationCache(context)

        assertFalse(dir1.exists())
        verify { failing2.deleteRecursively() }
        verify { failing3.deleteRecursively() }
        verify { context.getString(R.string.cache_cleared_error) }
        verify { Toast.makeText(context, "error", Toast.LENGTH_SHORT) }
        println("🏁 [TEST DONE] clearApplicationCache handles partial deletion")
    }
}
