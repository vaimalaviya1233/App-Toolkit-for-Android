package com.d4rk.android.libs.apptoolkit.app.help.domain.usecases

import android.app.Application
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.Errors
import com.d4rk.android.libs.apptoolkit.core.utils.constants.links.AppLinks
import com.d4rk.android.libs.apptoolkit.core.utils.extensions.toError
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.IntentsHelper
import com.google.android.gms.tasks.Task
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class RequestReviewFlowUseCase(private val application : Application) {
    operator fun invoke() : Flow<DataState<ReviewInfo , Errors>> = flow {
        runCatching {
            suspendCancellableCoroutine { continuation : CancellableContinuation<ReviewInfo> ->
                val reviewManager : ReviewManager = ReviewManagerFactory.create(application)
                val request : Task<ReviewInfo> = reviewManager.requestReviewFlow()
                val packageName : String = application.packageName

                request.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resume(value = task.result)
                    }
                    else {
                        continuation.resumeWithException(exception = Exception("Failed to request review flow"))
                    }
                }.addOnFailureListener { throwable : Exception ->
                    IntentsHelper.openUrl(context = application , url = "${AppLinks.PLAY_STORE_APP}$packageName${AppLinks.PLAY_STORE_APP_REVIEWS_SUFFIX}")
                    continuation.resumeWithException(exception = throwable)
                }
            }
        }.onSuccess { reviewInfo : ReviewInfo ->
            emit(value = DataState.Success(data = reviewInfo))
        }.onFailure { throwable : Throwable ->
            emit(value = DataState.Error(error = throwable.toError(default = Errors.UseCase.FAILED_TO_REQUEST_REVIEW)))
        }
    }
}