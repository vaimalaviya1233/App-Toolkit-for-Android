package com.d4rk.android.apps.apptoolkit.app.main.ui.components.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import com.d4rk.android.apps.apptoolkit.core.data.datastore.DataStore
import com.d4rk.android.apps.apptoolkit.app.main.utils.constants.NavigationRoutes
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

class AppNavigationHostTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var navController: TestNavHostController

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun startDestinationUsesStartupRouteFromDataStore() {
        val dataStore = mockk<DataStore> {
            every { getStartupPage(default = any()) } returns flowOf(NavigationRoutes.ROUTE_FAVORITE_APPS)
        }
        startKoin { modules(module { single<DataStore> { dataStore } }) }

        composeRule.setContent {
            val context = LocalContext.current
            navController = TestNavHostController(context)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            AppNavigationHost(
                navController = navController,
                snackbarHostState = SnackbarHostState(),
                paddingValues = PaddingValues()
            )
        }

        composeRule.runOnIdle {
            assertThat(navController.graph.startDestinationRoute).isEqualTo(NavigationRoutes.ROUTE_FAVORITE_APPS)
        }
    }

    @Test
    fun blankStartupRouteDefaultsToAppsList() {
        val dataStore = mockk<DataStore> {
            every { getStartupPage(default = any()) } returns flowOf("")
        }
        startKoin { modules(module { single<DataStore> { dataStore } }) }

        composeRule.setContent {
            val context = LocalContext.current
            navController = TestNavHostController(context)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            AppNavigationHost(
                navController = navController,
                snackbarHostState = SnackbarHostState(),
                paddingValues = PaddingValues()
            )
        }

        composeRule.runOnIdle {
            assertThat(navController.graph.startDestinationRoute).isEqualTo(NavigationRoutes.ROUTE_APPS_LIST)
        }
    }
}
