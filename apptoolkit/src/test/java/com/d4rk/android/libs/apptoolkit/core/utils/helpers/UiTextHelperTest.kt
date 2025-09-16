package com.d4rk.android.libs.apptoolkit.core.utils.helpers

import android.content.Context
import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.EmptyApplier
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.Recomposer
import androidx.compose.ui.platform.LocalContext
import io.mockk.*
import java.util.MissingFormatArgumentException
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class UiTextHelperTest {

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `dynamic string returns raw content when using context`() {
        val helper = UiTextHelper.DynamicString("Hello World")
        val context = mockk<Context>(relaxed = true)

        val result = helper.asString(context)

        assertEquals("Hello World", result)
    }

    @Test
    fun `string resource resolves with formatting arguments when using context`() {
        val resourceId = 101
        val helper = UiTextHelper.StringResource(resourceId, listOf("World"))
        val context = createStringResolvingContext { id, args ->
            assertEquals(resourceId, id)
            assertEquals(listOf("World"), args)
            "Hello ${args.first()}"
        }

        val result = helper.asString(context)

        assertEquals("Hello World", result)
    }

    @Test
    fun `string resource throws when resource is missing using context`() {
        val resourceId = 202
        val expected = Resources.NotFoundException("Missing $resourceId")
        val helper = UiTextHelper.StringResource(resourceId)
        val context = createStringResolvingContext { _, _ -> throw expected }

        val thrown = assertThrows(Resources.NotFoundException::class.java) {
            helper.asString(context)
        }

        assertEquals(expected.message, thrown.message)
    }

    @Test
    fun `string resource throws when format arguments are incorrect using context`() {
        val resourceId = 303
        val expected = MissingFormatArgumentException("name")
        val helper = UiTextHelper.StringResource(resourceId, listOf("only one"))
        val context = createStringResolvingContext { _, _ -> throw expected }

        val thrown = assertThrows(MissingFormatArgumentException::class.java) {
            helper.asString(context)
        }

        assertEquals(expected.message, thrown.message)
    }

    @Test
    fun `dynamic string returns raw content in composable`() = runTest {
        val helper = UiTextHelper.DynamicString("Hello Compose")
        val context = mockk<Context>(relaxed = true)
        var result: String? = null

        runComposableWithContext(context) {
            result = helper.asString()
        }

        assertEquals("Hello Compose", result)
    }

    @Test
    fun `string resource resolves with formatting arguments in composable`() = runTest {
        val resourceId = 404
        val helper = UiTextHelper.StringResource(resourceId, listOf("Compose"))
        val context = createStringResolvingContext { id, args ->
            assertEquals(resourceId, id)
            assertEquals(listOf("Compose"), args)
            "Hello ${args.first()}"
        }
        var result: String? = null

        runComposableWithContext(context) {
            result = helper.asString()
        }

        assertEquals("Hello Compose", result)
    }

    @Test
    fun `string resource throws when resource is missing in composable`() = runTest {
        val resourceId = 505
        val expected = Resources.NotFoundException("Missing $resourceId")
        val helper = UiTextHelper.StringResource(resourceId)
        val context = createStringResolvingContext { _, _ -> throw expected }

        val failure = runCatching {
            runComposableWithContext(context) {
                helper.asString()
            }
        }.exceptionOrNull()

        assertTrue(failure is Resources.NotFoundException)
        assertEquals(expected.message, failure?.message)
    }

    @Test
    fun `string resource throws when format arguments are incorrect in composable`() = runTest {
        val resourceId = 606
        val expected = MissingFormatArgumentException("value")
        val helper = UiTextHelper.StringResource(resourceId, listOf("Compose"))
        val context = createStringResolvingContext { _, _ -> throw expected }

        val failure = runCatching {
            runComposableWithContext(context) {
                helper.asString()
            }
        }.exceptionOrNull()

        assertTrue(failure is MissingFormatArgumentException)
        assertEquals(expected.message, failure?.message)
    }

    private fun createStringResolvingContext(
        resolver: (Int, List<Any?>) -> String
    ): Context {
        val context = mockk<Context>()
        every { context.getString(any(), *anyVararg()) } answers {
            val resourceId = firstArg<Int>()
            val arguments = args.drop(1)
            resolver(resourceId, arguments)
        }
        every { context.getString(any()) } answers {
            val resourceId = firstArg<Int>()
            resolver(resourceId, emptyList())
        }
        return context
    }

    private suspend fun TestScope.runComposableWithContext(
        context: Context,
        block: @Composable () -> Unit
    ) {
        val recomposer = Recomposer(coroutineContext + ImmediateFrameClock)
        val recomposeJob = launch(ImmediateFrameClock) {
            recomposer.runRecomposeAndApplyChanges()
        }
        val composition = Composition(EmptyApplier(), recomposer)
        try {
            composition.setContent {
                CompositionLocalProvider(LocalContext provides context) {
                    block()
                }
            }
            advanceUntilIdle()
        } finally {
            composition.dispose()
            recomposeJob.cancelAndJoin()
        }
    }

    private object ImmediateFrameClock : MonotonicFrameClock {
        override suspend fun <R> withFrameNanos(onFrame: (Long) -> R): R = onFrame(0L)
    }
}
