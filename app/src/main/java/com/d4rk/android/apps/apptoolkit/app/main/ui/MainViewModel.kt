package com.d4rk.android.apps.apptoolkit.app.main.ui

import androidx.lifecycle.viewModelScope
import com.d4rk.android.apps.apptoolkit.app.main.domain.action.MainAction
import com.d4rk.android.apps.apptoolkit.app.main.domain.action.MainEvent
import com.d4rk.android.apps.apptoolkit.app.main.domain.model.ui.UiMainScreen
import com.d4rk.android.apps.apptoolkit.app.main.domain.repository.MainRepository
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.successData
import com.d4rk.android.libs.apptoolkit.core.ui.base.ScreenViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainViewModel(
    private val repository: MainRepository
) : ScreenViewModel<UiMainScreen , MainEvent , MainAction>(initialState = UiStateScreen(data = UiMainScreen())) {

    init {
        onEvent(event = MainEvent.LoadNavigation)
    }

    override fun onEvent(event : MainEvent) {
        when (event) {
            is MainEvent.LoadNavigation -> loadNavigationItems()
        }
    }

    private fun loadNavigationItems() {
        repository.getNavigationDrawerItems()
            .onEach { items ->
                screenState.successData { copy(navigationDrawerItems = items) }
            }
            .catch { error ->
                screenState.successData {
                    copy(
                        showSnackbar = true,
                        snackbarMessage = error.message ?: "Failed to load navigation"
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}

