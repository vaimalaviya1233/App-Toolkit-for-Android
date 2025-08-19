package com.d4rk.android.libs.apptoolkit.app.onboarding.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class OnboardingViewModel : ViewModel() {
    var currentTabIndex by mutableIntStateOf(0)
}
