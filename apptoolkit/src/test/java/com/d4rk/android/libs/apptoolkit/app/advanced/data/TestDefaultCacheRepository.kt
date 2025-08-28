package com.d4rk.android.libs.apptoolkit.app.advanced.data

import android.content.Context
import com.d4rk.android.libs.apptoolkit.core.domain.model.Result
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlin.io.path.createTempDirectory
import kotlin.test.assertFalse
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.runTest
import org.junit.Test

class TestDefaultCacheRepository {

    @Test
    fun `clearCache deletes cache directories`() = runTest {
        println("\uD83D\uDE80 [TEST] clearCache deletes cache directories")
        val dir1 = createTempDirectory().toFile()
        val dir2 = createTempDirectory().toFile()
        val dir3 = createTempDirectory().toFile()

        val context = mockk<Context>()
        every { context.cacheDir } returns dir1
        every { context.codeCacheDir } returns dir2
        every { context.filesDir } returns dir3

        val repository = DefaultCacheRepository(context)
        val result = repository.clearCache().single()

        assertThat(result).isInstanceOf(Result.Success::class.java)
        assertFalse(dir1.exists())
        assertFalse(dir2.exists())
        assertFalse(dir3.exists())
        println("\uD83C\uDFC1 [TEST DONE] clearCache deletes cache directories")
    }

    @Test
    fun `clearCache returns false when deletion fails`() = runTest {
        println("\uD83D\uDE80 [TEST] clearCache returns false when deletion fails")
        val dir1 = createTempDirectory().toFile()
        val failing = mockk<java.io.File>()
        every { failing.deleteRecursively() } returns false
        every { failing.exists() } returns true
        val dir3 = createTempDirectory().toFile()

        val context = mockk<Context>()
        every { context.cacheDir } returns dir1
        every { context.codeCacheDir } returns failing
        every { context.filesDir } returns dir3

        val repository = DefaultCacheRepository(context)
        val result = repository.clearCache().single()

        assertThat(result).isInstanceOf(Result.Error::class.java)
        assertFalse(dir1.exists())
        assertFalse(dir3.exists())
        println("\uD83C\uDFC1 [TEST DONE] clearCache returns false when deletion fails")
    }

    @Test
    fun `clearCache emits error when directory inaccessible`() = runTest {
        println("\uD83D\uDE80 [TEST] clearCache emits error when directory inaccessible")
        val context = mockk<Context>()
        every { context.cacheDir } throws SecurityException("denied")

        val repository = DefaultCacheRepository(context)
        val result = repository.clearCache().single()
        assertThat(result).isInstanceOf(Result.Error::class.java)
        println("\uD83C\uDFC1 [TEST DONE] clearCache emits error when directory inaccessible")
    }

    @Test
    fun `clearCache handles missing directories`() = runTest {
        println("\uD83D\uDE80 [TEST] clearCache handles missing directories")
        val dir1 = createTempDirectory().toFile().also { it.deleteRecursively() }
        val dir2 = createTempDirectory().toFile().also { it.deleteRecursively() }
        val dir3 = createTempDirectory().toFile().also { it.deleteRecursively() }

        val context = mockk<Context>()
        every { context.cacheDir } returns dir1
        every { context.codeCacheDir } returns dir2
        every { context.filesDir } returns dir3

        val repository = DefaultCacheRepository(context)
        val result = repository.clearCache().single()

        assertThat(result).isInstanceOf(Result.Success::class.java)
        println("\uD83C\uDFC1 [TEST DONE] clearCache handles missing directories")
    }

    @Test
    fun `clearCache emits error when io exception`() = runTest {
        println("\uD83D\uDE80 [TEST] clearCache emits error when io exception")
        val dir1 = createTempDirectory().toFile()
        val failing = mockk<java.io.File>()
        every { failing.deleteRecursively() } throws java.io.IOException("io")
        every { failing.exists() } returns true
        val dir3 = createTempDirectory().toFile()

        val context = mockk<Context>()
        every { context.cacheDir } returns dir1
        every { context.codeCacheDir } returns failing
        every { context.filesDir } returns dir3

        val repository = DefaultCacheRepository(context)
        val result = repository.clearCache().single()
        assertThat(result).isInstanceOf(Result.Error::class.java)
        println("\uD83C\uDFC1 [TEST DONE] clearCache emits error when io exception")
    }
}
