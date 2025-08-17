package com.d4rk.android.libs.apptoolkit.core.utils.helpers

import android.app.Activity
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object ReviewHelper {

    fun launchInAppReviewIfEligible(
        activity: Activity,
        sessionCount: Int,
        hasPromptedBefore: Boolean,
        scope: CoroutineScope,
        onReviewLaunched: () -> Unit
    ) {
        if (sessionCount < 3 || hasPromptedBefore) return
        scope.launch {
            val launched = launchReview(activity)
            if (launched) {
                onReviewLaunched()
            }
        }
    }

    fun forceLaunchInAppReview(activity: Activity, scope: CoroutineScope) {
        scope.launch {
            launchReview(activity)
        }
    }

    suspend fun launchReview(activity: Activity): Boolean {
        val reviewManager = ReviewManagerFactory.create(activity)
        return runCatching {
            val reviewInfo = reviewManager.requestReviewFlow().await()
            reviewManager.launchReviewFlow(activity, reviewInfo).await()
            true
        }.getOrDefault(false)
    }
}