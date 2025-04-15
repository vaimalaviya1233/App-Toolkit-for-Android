package com.d4rk.android.libs.apptoolkit.app.help.domain.events

import android.app.Activity
import com.d4rk.android.libs.apptoolkit.core.ui.base.handling.UiEvent
import com.google.android.play.core.review.ReviewInfo

sealed interface HelpEvents : UiEvent {
    data object LoadHelp : HelpEvents
    data object RequestReview : HelpEvents
    data class LaunchReviewFlow(val activity : Activity , val reviewInfo : ReviewInfo) : HelpEvents
}
