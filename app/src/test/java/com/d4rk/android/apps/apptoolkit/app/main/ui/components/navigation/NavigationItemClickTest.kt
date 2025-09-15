package com.d4rk.android.apps.apptoolkit.app.main.ui.components.navigation

import android.content.Context
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.material3.DrawerState
import com.d4rk.android.libs.apptoolkit.app.main.utils.constants.NavigationDrawerRoutes
import com.d4rk.android.libs.apptoolkit.app.settings.settings.ui.SettingsActivity
import com.d4rk.android.libs.apptoolkit.app.help.ui.HelpActivity
import com.d4rk.android.libs.apptoolkit.core.domain.model.navigation.NavigationDrawerItem
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.IntentsHelper
import io.mockk.mockkObject
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.every
import io.mockk.verify
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.Runs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class NavigationItemClickTest {

    private val context = mockk<Context>(relaxed = true)

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

    @BeforeEach
    fun setup() {
        mockkObject(IntentsHelper)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `settings route opens settings activity`() {
        every { IntentsHelper.openActivity(context, SettingsActivity::class.java) } returns true

        handleNavigationItemClick(
            context = context,
            item = navigationItem(NavigationDrawerRoutes.ROUTE_SETTINGS)
        )

        verify { IntentsHelper.openActivity(context, SettingsActivity::class.java) }
    }

    @Test
    fun `help and feedback route opens help activity`() {
        every { IntentsHelper.openActivity(context, HelpActivity::class.java) } returns true

        handleNavigationItemClick(
            context = context,
            item = navigationItem(NavigationDrawerRoutes.ROUTE_HELP_AND_FEEDBACK)
        )

        verify { IntentsHelper.openActivity(context, HelpActivity::class.java) }
    }

    @Test
    fun `updates route triggers changelog callback`() {
        var invoked = false

        handleNavigationItemClick(
            context = context,
            item = navigationItem(NavigationDrawerRoutes.ROUTE_UPDATES),
            onChangelogRequested = { invoked = true }
        )

        assertTrue(invoked)
    }

    @Test
    fun `share route invokes share app`() {
        every {
            IntentsHelper.shareApp(
                context,
                com.d4rk.android.libs.apptoolkit.R.string.summary_share_message
            )
        } returns true

        handleNavigationItemClick(
            context = context,
            item = navigationItem(NavigationDrawerRoutes.ROUTE_SHARE)
        )

        verify {
            IntentsHelper.shareApp(
                context,
                com.d4rk.android.libs.apptoolkit.R.string.summary_share_message
            )
        }
    }

    @Test
    fun `drawer state is closed when provided`() = runTest {
        every { IntentsHelper.openActivity(context, SettingsActivity::class.java) } returns true
        val drawerState = mockk<DrawerState>()
        coEvery { drawerState.close() } just Runs

        handleNavigationItemClick(
            context = context,
            item = navigationItem(NavigationDrawerRoutes.ROUTE_SETTINGS),
            drawerState = drawerState,
            coroutineScope = this
        )
        advanceUntilIdle()

        coVerify { drawerState.close() }
    }
}

