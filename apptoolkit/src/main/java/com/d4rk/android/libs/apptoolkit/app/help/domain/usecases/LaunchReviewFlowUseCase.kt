package com.d4rk.android.libs.apptoolkit.app.help.domain.usecases

import android.app.Activity
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.Errors
import com.d4rk.android.libs.apptoolkit.core.utils.extensions.toError
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LaunchReviewFlowUseCase {
    operator fun invoke(param : Pair<Activity , ReviewInfo>) : Flow<DataState<Unit , Errors>> = flow {
        runCatching {
            val (activity : Activity , reviewInfo : ReviewInfo) = param
            val reviewManager : ReviewManager = ReviewManagerFactory.create(activity)
            reviewManager.launchReviewFlow(activity , reviewInfo)
        }.onSuccess {
            emit(value = DataState.Success(data = Unit))
        }.onFailure { throwable ->
            emit(value = DataState.Error(error = throwable.toError(default = Errors.UseCase.FAILED_TO_LAUNCH_REVIEW)))
        }
    }
}