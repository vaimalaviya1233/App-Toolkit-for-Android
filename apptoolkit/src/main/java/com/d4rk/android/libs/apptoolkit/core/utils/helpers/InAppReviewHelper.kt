package com.d4rk.android.libs.apptoolkit.core.utils.helpers

import android.app.Activity
import android.widget.Toast
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.data.datastore.CommonDataStore
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.IntentsHelper
import com.google.android.gms.tasks.Task
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object InAppReviewHelper {
    suspend fun launchReview(activity: Activity, dataStore: CommonDataStore) {
        val hasReviewed = dataStore.reviewDone.first()
        if (hasReviewed) {
            withContext(Dispatchers.Main) {
                Toast.makeText(activity, activity.getString(R.string.toast_review_already_done), Toast.LENGTH_SHORT).show()
            }
            return
        }

        val reviewManager: ReviewManager = ReviewManagerFactory.create(activity)
        val reviewInfo = try {
            suspendCancellableCoroutine { cont ->
                val request: Task<ReviewInfo> = reviewManager.requestReviewFlow()
                request.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        cont.resume(task.result)
                    } else {
                        cont.resumeWithException(Exception("Failed to request review"))
                    }
                }.addOnFailureListener { cont.resumeWithException(it) }
            }
        } catch (e: Exception) {
            IntentsHelper.openPlayStoreForApp(activity, activity.packageName)
            return
        }

        runCatching {
            suspendCancellableCoroutine { cont ->
                val launch = reviewManager.launchReviewFlow(activity, reviewInfo)
                launch.addOnCompleteListener { cont.resume(Unit) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
        }.onFailure {
            IntentsHelper.openPlayStoreForApp(activity, activity.packageName)
        }
        dataStore.saveReviewDone(true)
    }
}
