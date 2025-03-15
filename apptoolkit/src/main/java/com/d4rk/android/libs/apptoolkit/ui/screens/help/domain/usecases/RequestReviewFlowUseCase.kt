package com.d4rk.android.libs.apptoolkit.ui.screens.help.domain.usecases

import android.app.Application
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.Errors
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.IntentsHelper
import com.google.android.play.core.review.ReviewInfo
import com.d4rk.android.libs.apptoolkit.core.utils.extensions.toError
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class RequestReviewFlowUseCase(private val application: Application) {
    operator fun invoke() : Flow<DataState<ReviewInfo, Errors>> = flow { // FIXME: Parameter 'param' is never used
        runCatching {
            withContext(Dispatchers.IO) {
                suspendCancellableCoroutine { continuation ->
                    val reviewManager = ReviewManagerFactory.create(application)
                    val request = reviewManager.requestReviewFlow()
                    val packageName = application.packageName

                    request.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            continuation.resume(task.result)
                        } else {
                            continuation.resumeWithException(Exception("Failed to request review flow"))
                        }
                    }.addOnFailureListener { throwable ->
                        IntentsHelper.openUrl(
                            context = application,
                            url = "https://play.google.com/store/apps/details?id=$packageName&showAllReviews=true"
                        )
                        continuation.resumeWithException(throwable)
                    }
                }
            }
        }.onSuccess { reviewInfo ->
            emit(DataState.Success(data = reviewInfo))
        }.onFailure { throwable ->
            emit(DataState.Error(error = throwable.toError(default = Errors.UseCase.FAILED_TO_REQUEST_REVIEW)))
        }
    }
}