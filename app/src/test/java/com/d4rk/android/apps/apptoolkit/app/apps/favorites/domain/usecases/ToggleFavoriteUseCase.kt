package com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.usecases

import kotlinx.coroutines.flow.MutableSharedFlow

class ToggleFavoriteUseCase {
    val toggles = MutableSharedFlow<String>()
    var shouldFail: Throwable? = null
    suspend operator fun invoke(pkg: String) {
        toggles.emit(pkg)
        shouldFail?.let { throw it }
    }
}
