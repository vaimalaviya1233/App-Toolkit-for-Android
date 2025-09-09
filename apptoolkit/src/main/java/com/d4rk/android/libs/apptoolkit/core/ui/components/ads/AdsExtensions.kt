package com.d4rk.android.libs.apptoolkit.core.ui.components.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d4rk.android.libs.apptoolkit.data.datastore.CommonDataStore

@Composable
fun rememberAdsEnabled(): Boolean {
    val context = LocalContext.current
    val dataStore: CommonDataStore = remember { CommonDataStore.getInstance(context) }
    return remember { dataStore.ads(default = true) }
        .collectAsStateWithLifecycle(initialValue = true).value
}