package com.d4rk.android.libs.apptoolkit.app.help.domain.actions

import android.app.Activity
import com.d4rk.android.libs.apptoolkit.core.ui.base.handling.UiEvent
import com.google.android.play.core.review.ReviewInfo

sealed interface HelpEvent : UiEvent {
    data object LoadHelp : HelpEvent
    data object RequestReview : HelpEvent
    data class LaunchReviewFlow(val activity : Activity , val reviewInfo : ReviewInfo) : HelpEvent
}
