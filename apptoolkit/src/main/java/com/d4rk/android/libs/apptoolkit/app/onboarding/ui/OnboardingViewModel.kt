package com.d4rk.android.libs.apptoolkit.app.onboarding.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.d4rk.android.libs.apptoolkit.app.onboarding.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for handling onboarding state and actions.
 *
 * Exposes [uiState] as a [StateFlow] following unidirectional data flow
 * and delegates data operations to an [OnboardingRepository].
 */
class OnboardingViewModel(
    private val repository: OnboardingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun updateCurrentTab(index: Int) {
        _uiState.update { it.copy(currentTabIndex = index) }
    }

    fun completeOnboarding(onFinished: () -> Unit) {
        viewModelScope.launch {
            repository.setOnboardingCompleted()
            onFinished()
        }
    }

    companion object {
        fun provideFactory(repository: OnboardingRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return OnboardingViewModel(repository) as T
                }
            }
    }
}
