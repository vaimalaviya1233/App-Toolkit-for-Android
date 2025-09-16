package com.d4rk.android.libs.apptoolkit.core.utils.helpers

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import com.d4rk.android.libs.apptoolkit.R
import com.google.common.truth.Truth.assertThat
import java.util.MissingFormatArgumentException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.assertThrows
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.junit5.RobolectricExtension

@ExtendWith(RobolectricExtension::class)
class UiTextHelperTest {

    @Test
    fun `DynamicString asString with context returns raw text`() {
        val context = applicationContext()
        val expected = "Plain text"

        val result = UiTextHelper.DynamicString(expected).asString(context)

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `StringResource asString with context resolves resource and arguments`() {
        val context = applicationContext()
        val placeholder = "https://example.com"
        val uiText = UiTextHelper.StringResource(
            resourceId = R.string.summary_share_message,
            arguments = listOf(placeholder)
        )

        val result = uiText.asString(context)

        assertThat(result).isEqualTo(context.getString(R.string.summary_share_message, placeholder))
    }

    @Test
    fun `StringResource asString with context throws when resource missing`() {
        val context = applicationContext()
        val uiText = UiTextHelper.StringResource(resourceId = Int.MAX_VALUE)

        assertThrows<Resources.NotFoundException> {
            uiText.asString(context)
        }
    }

    @Test
    fun `StringResource asString with context propagates formatting exceptions`() {
        val context = applicationContext()
        val uiText = UiTextHelper.StringResource(resourceId = R.string.summary_share_message)

        assertThrows<MissingFormatArgumentException> {
            uiText.asString(context)
        }
    }

    @Test
    fun `DynamicString composable asString returns raw text without touching context`() {
        val fakeContext = recordingContext()
        val expected = "Compose text"

        val result = evaluateComposable(fakeContext) {
            UiTextHelper.DynamicString(expected).asString()
        }

        assertThat(result).isEqualTo(expected)
        assertThat(fakeContext.lastResourceId).isNull()
        assertThat(fakeContext.lastFormatArgs).isNull()
    }

    @Test
    fun `StringResource composable asString resolves resource using fake context`() {
        val fakeContext = recordingContext()
        val placeholder = "https://example.com"
        val uiText = UiTextHelper.StringResource(
            resourceId = R.string.summary_share_message,
            arguments = listOf(placeholder)
        )

        val expected = fakeContext.baseContext.getString(R.string.summary_share_message, placeholder)

        val result = evaluateComposable(fakeContext) {
            uiText.asString()
        }

        assertThat(result).isEqualTo(expected)
        assertThat(fakeContext.lastResourceId).isEqualTo(R.string.summary_share_message)
        assertThat(fakeContext.lastFormatArgs).containsExactly(placeholder)
    }

    @Test
    fun `StringResource composable asString throws when resource missing`() {
        val fakeContext = recordingContext()
        val uiText = UiTextHelper.StringResource(resourceId = Int.MAX_VALUE)

        assertThrows<Resources.NotFoundException> {
            evaluateComposable(fakeContext) { uiText.asString() }
        }
        assertThat(fakeContext.lastResourceId).isEqualTo(Int.MAX_VALUE)
    }

    @Test
    fun `StringResource composable asString propagates formatting exceptions`() {
        val fakeContext = recordingContext()
        val uiText = UiTextHelper.StringResource(resourceId = R.string.summary_share_message)

        assertThrows<MissingFormatArgumentException> {
            evaluateComposable(fakeContext) { uiText.asString() }
        }
        assertThat(fakeContext.lastResourceId).isEqualTo(R.string.summary_share_message)
        assertThat(fakeContext.lastFormatArgs).isEmpty()
    }

    private fun applicationContext(): Context = RuntimeEnvironment.getApplication()

    private fun recordingContext(): RecordingContext = RecordingContext(applicationContext())

    private fun <T> evaluateComposable(context: Context, block: @Composable () -> T): T {
        val composeView = ComposeView(context)
        var result: Result<T>? = null
        composeView.setContent {
            result = runCatching { block() }
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        composeView.disposeComposition()
        return result!!.getOrThrow()
    }

    private class RecordingContext(base: Context) : ContextWrapper(base) {
        var lastResourceId: Int? = null
            private set
        var lastFormatArgs: List<Any?>? = null
            private set

        override fun getString(id: Int): String {
            lastResourceId = id
            lastFormatArgs = emptyList()
            return super.getString(id)
        }

        override fun getString(id: Int, vararg formatArgs: Any): String {
            lastResourceId = id
            lastFormatArgs = formatArgs.toList()
            return super.getString(id, *formatArgs)
        }
    }
}
