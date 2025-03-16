package com.d4rk.android.libs.apptoolkit.app.help.domain.actions

import android.app.Activity
import com.google.android.play.core.review.ReviewInfo

sealed class HelpAction {
    data object LoadHelp : HelpAction()
    data object RequestReview : HelpAction()
    data class LaunchReviewFlow(val activity: Activity , val reviewInfo: ReviewInfo) : HelpAction()
}