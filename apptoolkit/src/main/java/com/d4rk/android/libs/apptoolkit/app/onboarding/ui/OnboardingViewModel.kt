package com.d4rk.android.libs.apptoolkit.app.onboarding.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d4rk.android.libs.apptoolkit.data.datastore.CommonDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OnboardingViewModel : ViewModel() {
    var currentTabIndex by mutableIntStateOf(0)
        private set

    fun updateCurrentTab(index: Int) {
        currentTabIndex = index
    }

    fun completeOnboarding(context: Context, onFinished: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                CommonDataStore.getInstance(context).saveStartup(isFirstTime = false)
            }
            onFinished()
        }
    }
}

