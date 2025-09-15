package com.d4rk.android.apps.apptoolkit.app.main.ui.components.navigation

import android.content.Context
import androidx.compose.material3.DrawerState
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.d4rk.android.libs.apptoolkit.app.help.ui.HelpActivity
import com.d4rk.android.libs.apptoolkit.app.main.utils.constants.NavigationDrawerRoutes
import com.d4rk.android.libs.apptoolkit.app.settings.settings.ui.SettingsActivity
import com.d4rk.android.libs.apptoolkit.core.domain.model.navigation.NavigationDrawerItem
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.IntentsHelper
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import java.util.stream.Stream
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

@OptIn(ExperimentalCoroutinesApi::class)
class AppNavigationHostTest {

    private val context = mockk<Context>(relaxed = true)

    @BeforeEach
    fun setUp() {
        mockkObject(IntentsHelper)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @ParameterizedTest(name = "handleNavigationItemClick triggers expected helper for {0}")
    @MethodSource("navigationRoutesProvider")
    fun `handleNavigationItemClick routes invoke expected helpers`(route: String) {
        var changelogInvoked = false

        when (route) {
            NavigationDrawerRoutes.ROUTE_SETTINGS ->
                every { IntentsHelper.openActivity(context, SettingsActivity::class.java) } returns true

            NavigationDrawerRoutes.ROUTE_HELP_AND_FEEDBACK ->
                every { IntentsHelper.openActivity(context, HelpActivity::class.java) } returns true

            NavigationDrawerRoutes.ROUTE_SHARE ->
                every {
                    IntentsHelper.shareApp(
                        context,
                        com.d4rk.android.libs.apptoolkit.R.string.summary_share_message
                    )
                } returns true
        }

        handleNavigationItemClick(
            context = context,
            item = navigationItem(route),
            onChangelogRequested = { changelogInvoked = true }
        )

        when (route) {
            NavigationDrawerRoutes.ROUTE_SETTINGS -> {
                verify { IntentsHelper.openActivity(context, SettingsActivity::class.java) }
                assertFalse(changelogInvoked)
            }

            NavigationDrawerRoutes.ROUTE_HELP_AND_FEEDBACK -> {
                verify { IntentsHelper.openActivity(context, HelpActivity::class.java) }
                assertFalse(changelogInvoked)
            }

            NavigationDrawerRoutes.ROUTE_UPDATES ->
                assertTrue(changelogInvoked)

            NavigationDrawerRoutes.ROUTE_SHARE -> {
                verify {
                    IntentsHelper.shareApp(
                        context,
                        com.d4rk.android.libs.apptoolkit.R.string.summary_share_message
                    )
                }
                assertFalse(changelogInvoked)
            }
        }
    }

    @Test
    fun `drawer state is closed when coroutine scope provided`() {
        val drawerState = mockk<DrawerState>()
        coEvery { drawerState.close() } just Runs
        every { IntentsHelper.openActivity(context, SettingsActivity::class.java) } returns true
        val coroutineScope = TestScope(StandardTestDispatcher())

        handleNavigationItemClick(
            context = context,
            item = navigationItem(NavigationDrawerRoutes.ROUTE_SETTINGS),
            drawerState = drawerState,
            coroutineScope = coroutineScope
        )

        coroutineScope.advanceUntilIdle()

        coVerify { drawerState.close() }
    }

    private fun navigationItem(route: String) = NavigationDrawerItem(
        title = 0,
        selectedIcon = ImageVector.Builder(
            name = "test",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).build(),
        route = route
    )

    companion object {
        @JvmStatic
        fun navigationRoutesProvider(): Stream<String> = Stream.of(
            NavigationDrawerRoutes.ROUTE_SETTINGS,
            NavigationDrawerRoutes.ROUTE_HELP_AND_FEEDBACK,
            NavigationDrawerRoutes.ROUTE_UPDATES,
            NavigationDrawerRoutes.ROUTE_SHARE
        )
    }
}
