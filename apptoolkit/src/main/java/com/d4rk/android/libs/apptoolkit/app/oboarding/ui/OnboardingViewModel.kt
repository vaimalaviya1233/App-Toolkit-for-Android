package com.d4rk.android.libs.apptoolkit.app.oboarding.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class OnboardingViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    companion object {
        private const val KEY_INDEX = "currentTabIndex"
    }

    var currentTabIndex by mutableStateOf(savedStateHandle.get<Int>(KEY_INDEX) ?: 0)
        private set

    fun onEvent(event: OnboardingEvent) {
        when (event) {
            is OnboardingEvent.SetIndex -> currentTabIndex = event.index
            OnboardingEvent.Next -> currentTabIndex++
            OnboardingEvent.Previous -> currentTabIndex--
            OnboardingEvent.Unknown -> Unit
        }
        savedStateHandle[KEY_INDEX] = currentTabIndex
    }

    constructor() : this(SavedStateHandle())
}

sealed interface OnboardingEvent {
    data object Next : OnboardingEvent
    data object Previous : OnboardingEvent
    data class SetIndex(val index: Int) : OnboardingEvent
    data object Unknown : OnboardingEvent
}
