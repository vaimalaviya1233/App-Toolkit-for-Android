package com.d4rk.android.libs.apptoolkit.data.core

import android.content.Context
import com.d4rk.android.libs.apptoolkit.app.support.billing.BillingRepository
import com.d4rk.android.libs.apptoolkit.core.di.DispatcherProvider
import com.d4rk.android.libs.apptoolkit.core.di.TestDispatchers
import com.d4rk.android.libs.apptoolkit.data.datastore.CommonDataStore
import com.google.firebase.Firebase
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class BaseCoreManagerTest {
    private lateinit var testDispatcher: StandardTestDispatcher
    private lateinit var dispatchers: DispatcherProvider
    private lateinit var context: Context

    @BeforeEach
    fun setUp() {
        testDispatcher = StandardTestDispatcher()
        dispatchers = TestDispatchers(testDispatcher)
        context = mockk(relaxed = true)
        resetAppLoaded()
        clearAllMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
        stopKoinIfStarted()
        resetAppLoaded()
    }

    @Test
    fun `onCreate initializes Firebase AppCheck and marks app as loaded`() = runTest(testDispatcher) {
        val manager = RecordingBaseCoreManager(dispatchers).also { it.attachForTest(context) }
        val firebaseMocks = mockFirebaseInitialization()

        assertFalse(BaseCoreManager.isAppLoaded)
        assertEquals(0, manager.initializeAppCalls)
        assertEquals(0, manager.finalizeCalls)

        manager.onCreate()
        assertFalse(BaseCoreManager.isAppLoaded)
        assertEquals(0, manager.initializeAppCalls)
        assertEquals(0, manager.finalizeCalls)

        advanceUntilIdle()

        assertEquals(1, manager.initializeAppCalls)
        assertEquals(1, manager.finalizeCalls)
        assertTrue(BaseCoreManager.isAppLoaded)

        verify(exactly = 1) { Firebase.initialize(context = manager) }
        verify(exactly = 1) { PlayIntegrityAppCheckProviderFactory.getInstance() }
        verify(exactly = 1) {
            firebaseMocks.appCheck.installAppCheckProviderFactory(firebaseMocks.providerFactory)
        }
    }

    @Test
    fun `onTerminate closes resources and cancels application scope`() = runTest(testDispatcher) {
        stopKoinIfStarted()
        val billingRepository = mockk<BillingRepository>(relaxed = true)
        val dataStore = mockk<CommonDataStore>(relaxed = true)

        startKoin {
            modules(module { single { billingRepository } })
        }

        mockkObject(CommonDataStore.Companion)
        every { CommonDataStore.getInstance(any()) } returns dataStore

        val manager = RecordingBaseCoreManager(dispatchers).also { it.attachForTest(context) }
        mockFirebaseInitialization()

        manager.onCreate()
        advanceUntilIdle()

        val applicationJob = manager.applicationScopeJob()
        assertFalse(applicationJob.isCancelled)

        manager.onTerminate()

        verify(exactly = 1) { billingRepository.close() }
        verify(exactly = 1) { CommonDataStore.getInstance(manager) }
        verify(exactly = 1) { dataStore.close() }
        assertTrue(applicationJob.isCancelled)
    }

    private data class FirebaseSetup(
        val appCheck: FirebaseAppCheck,
        val providerFactory: PlayIntegrityAppCheckProviderFactory,
    )

    private fun mockFirebaseInitialization(): FirebaseSetup {
        mockkStatic(Firebase::class)
        mockkStatic("com.google.firebase.appcheck.FirebaseAppCheckKt")
        mockkStatic(PlayIntegrityAppCheckProviderFactory::class)

        val appCheck = mockk<FirebaseAppCheck>(relaxed = true)
        val providerFactory = mockk<PlayIntegrityAppCheckProviderFactory>()

        every { Firebase.appCheck } returns appCheck
        every { PlayIntegrityAppCheckProviderFactory.getInstance() } returns providerFactory
        justRun { Firebase.initialize(any()) }
        justRun { appCheck.installAppCheckProviderFactory(any()) }

        return FirebaseSetup(appCheck, providerFactory)
    }

    private fun RecordingBaseCoreManager.applicationScopeJob(): Job {
        val field = BaseCoreManager::class.java.getDeclaredField("applicationScope").apply { isAccessible = true }
        val scope = field.get(this) as CoroutineScope
        return scope.coroutineContext[Job] ?: error("applicationScope Job was null")
    }

    private fun resetAppLoaded() {
        val companionClass = BaseCoreManager::class.java.declaredClasses.first { it.simpleName == "Companion" }
        val instanceField = companionClass.getDeclaredField("INSTANCE").apply { isAccessible = true }
        val companion = instanceField.get(null)
        val flagField = companionClass.getDeclaredField("isAppLoaded").apply { isAccessible = true }
        flagField.setBoolean(companion, false)
    }

    private fun stopKoinIfStarted() {
        runCatching { stopKoin() }
    }
}

private class RecordingBaseCoreManager(
    override val dispatchers: DispatcherProvider,
) : BaseCoreManager() {
    var initializeAppCalls: Int = 0
        private set
    var finalizeCalls: Int = 0
        private set

    fun attachForTest(context: Context) {
        attachBaseContext(context)
    }

    override suspend fun onInitializeApp() {
        initializeAppCalls += 1
    }

    override fun finalizeInitialization() {
        finalizeCalls += 1
        super.finalizeInitialization()
    }
}
