package com.d4rk.android.apps.apptoolkit.app.apps.common

import android.content.Context
import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.Recomposer
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.core.di.DispatcherProvider
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.AppInfoHelper
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.IntentsHelper
import io.mockk.anyConstructed
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.firstArg
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.secondArg
import io.mockk.thirdArg
import io.mockk.unmockkConstructor
import io.mockk.unmockkObject
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppActionsTest {

    private lateinit var context: Context
    private lateinit var fakeAppInfoHelper: FakeAppInfoHelper
    private lateinit var fakeIntentsHelper: FakeIntentsHelper

    @BeforeEach
    fun setUp() {
        context = mockk(relaxed = true)
        fakeAppInfoHelper = FakeAppInfoHelper().apply { setup() }
        fakeIntentsHelper = FakeIntentsHelper().apply { setup() }
    }

    @AfterEach
    fun tearDown() {
        fakeAppInfoHelper.tearDown()
        fakeIntentsHelper.tearDown()
        clearAllMocks()
    }

    @Test
    fun buildOnAppClick_whenAppInstalled_opensAppDirectly() = runTest {
        val dispatcherProvider = testDispatcherProvider()
        fakeAppInfoHelper.isInstalledResult = true
        fakeAppInfoHelper.openAppResult = true

        val appInfo = appInfo(packageName = "com.example.installed")
        lateinit var onAppClick: (AppInfo) -> Unit

        withComposable(
            content = { onAppClick = buildOnAppClick(dispatcherProvider, context) }
        ) {
            onAppClick(appInfo)
            advanceUntilIdle()
        }

        assertEquals(listOf(appInfo.packageName), fakeAppInfoHelper.isAppInstalledCalls.map { it.packageName })
        assertEquals(listOf(appInfo.packageName), fakeAppInfoHelper.openAppCalls.map { it.packageName })
        assertTrue(fakeIntentsHelper.playStoreRequests.isEmpty())
    }

    @Test
    fun buildOnAppClick_whenLaunchFails_fallsBackToPlayStore() = runTest {
        val dispatcherProvider = testDispatcherProvider()
        fakeAppInfoHelper.isInstalledResult = true
        fakeAppInfoHelper.openAppResult = false

        val appInfo = appInfo(packageName = "com.example.failure")
        lateinit var onAppClick: (AppInfo) -> Unit

        withComposable(
            content = { onAppClick = buildOnAppClick(dispatcherProvider, context) }
        ) {
            onAppClick(appInfo)
            advanceUntilIdle()
        }

        assertEquals(listOf(appInfo.packageName), fakeAppInfoHelper.isAppInstalledCalls.map { it.packageName })
        assertEquals(listOf(appInfo.packageName), fakeAppInfoHelper.openAppCalls.map { it.packageName })
        assertEquals(listOf(appInfo.packageName), fakeIntentsHelper.playStoreRequests.map { it.packageName })
    }

    @Test
    fun buildOnAppClick_whenAppNotInstalled_opensPlayStore() = runTest {
        val dispatcherProvider = testDispatcherProvider()
        fakeAppInfoHelper.isInstalledResult = false

        val appInfo = appInfo(packageName = "com.example.notinstalled")
        lateinit var onAppClick: (AppInfo) -> Unit

        withComposable(
            content = { onAppClick = buildOnAppClick(dispatcherProvider, context) }
        ) {
            onAppClick(appInfo)
            advanceUntilIdle()
        }

        assertEquals(listOf(appInfo.packageName), fakeAppInfoHelper.isAppInstalledCalls.map { it.packageName })
        assertTrue(fakeAppInfoHelper.openAppCalls.isEmpty())
        assertEquals(listOf(appInfo.packageName), fakeIntentsHelper.playStoreRequests.map { it.packageName })
    }

    @Test
    fun buildOnAppClick_withEmptyPackageName_takesNoAction() = runTest {
        val dispatcherProvider = testDispatcherProvider()

        val appInfo = appInfo(packageName = "")
        lateinit var onAppClick: (AppInfo) -> Unit

        withComposable(
            content = { onAppClick = buildOnAppClick(dispatcherProvider, context) }
        ) {
            onAppClick(appInfo)
            advanceUntilIdle()
        }

        assertTrue(fakeAppInfoHelper.isAppInstalledCalls.isEmpty())
        assertTrue(fakeAppInfoHelper.openAppCalls.isEmpty())
        assertTrue(fakeIntentsHelper.playStoreRequests.isEmpty())
    }

    @Test
    fun buildOnShareClick_usesExpectedShareIntent() = runTest {
        val appInfo = appInfo(packageName = "com.example.share")
        lateinit var onShareClick: (AppInfo) -> Unit

        withComposable(content = { onShareClick = buildOnShareClick(context) }) {
            onShareClick(appInfo)
        }

        val shareRequest = fakeIntentsHelper.shareRequests.single()
        assertEquals(context, shareRequest.context)
        assertEquals(R.string.summary_share_message, shareRequest.shareMessageRes)
        assertEquals(appInfo.packageName, shareRequest.packageName)
    }
}

private fun appInfo(packageName: String): AppInfo = AppInfo(
    name = "Test App",
    packageName = packageName,
    iconUrl = "https://example.com/icon.png",
)

private fun TestScope.testDispatcherProvider(): DispatcherProvider {
    val dispatcher = StandardTestDispatcher(testScheduler)
    return TestDispatcherProvider(dispatcher)
}

private class TestDispatcherProvider(
    private val dispatcher: CoroutineDispatcher,
) : DispatcherProvider {
    override val main: CoroutineDispatcher = dispatcher
    override val io: CoroutineDispatcher = dispatcher
    override val default: CoroutineDispatcher = dispatcher
    override val unconfined: CoroutineDispatcher = dispatcher
}

private suspend fun TestScope.withComposable(
    content: @Composable () -> Unit,
    block: suspend TestScope.() -> Unit,
) {
    val frameClock = TestFrameClock()
    val recomposer = Recomposer(coroutineContext + frameClock)
    val composition = Composition(NoOpApplier(), recomposer)
    val recomposerJob = launch { recomposer.runRecomposeAndApplyChanges() }
    try {
        composition.setContent(content)
        advanceUntilIdle()
        block()
        advanceUntilIdle()
    } finally {
        composition.dispose()
        recomposerJob.cancelAndJoin()
    }
}

private class NoOpApplier : AbstractApplier<Unit>(Unit) {
    override fun insertBottomUp(index: Int, instance: Unit) = Unit
    override fun insertTopDown(index: Int, instance: Unit) = Unit
    override fun move(from: Int, to: Int, count: Int) = Unit
    override fun remove(index: Int, count: Int) = Unit
    override fun onClear() = Unit
}

private class TestFrameClock : MonotonicFrameClock {
    override suspend fun <R> withFrameNanos(onFrame: (Long) -> R): R = onFrame(0L)
}

private class FakeAppInfoHelper {
    var isInstalledResult: Boolean = false
    var openAppResult: Boolean = false

    val isAppInstalledCalls = mutableListOf<AppCall>()
    val openAppCalls = mutableListOf<AppCall>()

    data class AppCall(val context: Context, val packageName: String)

    fun setup() {
        mockkConstructor(AppInfoHelper::class)
        coEvery { anyConstructed<AppInfoHelper>().isAppInstalled(any(), any()) } answers@{
            val context = firstArg<Context>()
            val packageName = secondArg<String>()
            isAppInstalledCalls += AppCall(context, packageName)
            return@answers isInstalledResult
        }
        coEvery { anyConstructed<AppInfoHelper>().openApp(any(), any()) } answers@{
            val context = firstArg<Context>()
            val packageName = secondArg<String>()
            openAppCalls += AppCall(context, packageName)
            return@answers openAppResult
        }
    }

    fun tearDown() {
        unmockkConstructor(AppInfoHelper::class)
    }
}

private class FakeIntentsHelper {
    val playStoreRequests = mutableListOf<PlayStoreRequest>()
    val shareRequests = mutableListOf<ShareRequest>()

    data class PlayStoreRequest(val context: Context, val packageName: String)
    data class ShareRequest(val context: Context, val shareMessageRes: Int, val packageName: String)

    fun setup() {
        mockkObject(IntentsHelper)
        every { IntentsHelper.openPlayStoreForApp(any(), any()) } answers@{
            val context = firstArg<Context>()
            val packageName = secondArg<String>()
            playStoreRequests += PlayStoreRequest(context, packageName)
            return@answers true
        }
        every { IntentsHelper.shareApp(any(), any(), any()) } answers@{
            val context = firstArg<Context>()
            val shareMessageRes = secondArg<Int>()
            val packageName = thirdArg<String>()
            shareRequests += ShareRequest(context, shareMessageRes, packageName)
            return@answers true
        }
    }

    fun tearDown() {
        unmockkObject(IntentsHelper)
    }
}
