package com.d4rk.android.libs.apptoolkit.core.utils.helpers

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.util.DisplayMetrics
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class ScreenHelperTest {

    @Test
    fun `isLandscape returns true when orientation is landscape`() {
        val context = createContext(
            orientation = Configuration.ORIENTATION_LANDSCAPE,
            screenWidthDp = 360
        )

        val result = ScreenHelper.isLandscape(context)

        assertThat(result).isTrue()
    }

    @Test
    fun `isLandscape returns false when orientation is portrait`() {
        val context = createContext(
            orientation = Configuration.ORIENTATION_PORTRAIT,
            screenWidthDp = 360
        )

        val result = ScreenHelper.isLandscape(context)

        assertThat(result).isFalse()
    }

    @Test
    fun `isTablet returns true when screen width is at least 600dp`() {
        val context = createContext(
            orientation = Configuration.ORIENTATION_PORTRAIT,
            screenWidthDp = 600
        )

        val result = ScreenHelper.isTablet(context)

        assertThat(result).isTrue()
    }

    @Test
    fun `isTablet returns false when screen width is below 600dp`() {
        val context = createContext(
            orientation = Configuration.ORIENTATION_PORTRAIT,
            screenWidthDp = 599
        )

        val result = ScreenHelper.isTablet(context)

        assertThat(result).isFalse()
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("landscapeOrTabletScenarios")
    fun `isLandscapeOrTablet returns expected result`(
        @Suppress("UNUSED_PARAMETER") description: String,
        orientation: Int,
        screenWidthDp: Int,
        expected: Boolean
    ) {
        val context = createContext(
            orientation = orientation,
            screenWidthDp = screenWidthDp
        )

        val result = ScreenHelper.isLandscapeOrTablet(context)

        assertThat(result).isEqualTo(expected)
    }

    private fun createContext(
        orientation: Int,
        screenWidthDp: Int
    ): Context {
        val configuration = Configuration().apply {
            this.orientation = orientation
            this.screenWidthDp = screenWidthDp
        }
        val displayMetrics = DisplayMetrics().apply {
            density = 2f
            densityDpi = (density * DisplayMetrics.DENSITY_DEFAULT).toInt()
            widthPixels = (screenWidthDp * density).toInt()
        }
        val resources = mockk<Resources>()
        every { resources.configuration } returns configuration
        every { resources.displayMetrics } returns displayMetrics

        val context = mockk<Context>()
        every { context.resources } returns resources
        return context
    }

    companion object {
        @JvmStatic
        fun landscapeOrTabletScenarios(): Stream<Arguments> = Stream.of(
            Arguments.of(
                "portrait phone",
                Configuration.ORIENTATION_PORTRAIT,
                360,
                false
            ),
            Arguments.of(
                "landscape phone",
                Configuration.ORIENTATION_LANDSCAPE,
                360,
                true
            ),
            Arguments.of(
                "portrait tablet",
                Configuration.ORIENTATION_PORTRAIT,
                800,
                true
            ),
            Arguments.of(
                "landscape tablet",
                Configuration.ORIENTATION_LANDSCAPE,
                800,
                true
            )
        )
    }
}
