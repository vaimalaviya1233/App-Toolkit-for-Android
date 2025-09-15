package com.d4rk.android.libs.apptoolkit.data.datastore

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.d4rk.android.libs.apptoolkit.core.di.TestDispatchers
import com.d4rk.android.libs.apptoolkit.core.utils.constants.datastore.DataStoreNamesConstants
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class CommonDataStoreThemeTest {

    @Test
    fun `saving theme mode emits value and updates state`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val commonDataStore = CommonDataStore(context, TestDispatchers(testScheduler))

        val job = launch {
            commonDataStore.themeMode.collect { mode ->
                commonDataStore.themeModeState.value = mode
            }
        }

        val expected = DataStoreNamesConstants.THEME_MODE_DARK
        commonDataStore.saveThemeMode(expected)
        advanceUntilIdle()

        val actual = commonDataStore.themeMode.first()
        assertThat(actual).isEqualTo(expected)
        assertThat(commonDataStore.themeModeState.value).isEqualTo(expected)

        job.cancel()
        commonDataStore.close()
    }
}

