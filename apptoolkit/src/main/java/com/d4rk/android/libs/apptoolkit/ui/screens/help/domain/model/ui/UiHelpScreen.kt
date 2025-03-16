package com.d4rk.android.libs.apptoolkit.ui.screens.help.domain.model.ui

import com.google.android.play.core.review.ReviewInfo

data class UiHelpScreen(
    var reviewInfo : ReviewInfo? = null , val questions : ArrayList<UiHelpQuestion> = ArrayList()
)

data class UiHelpQuestion(
    val question : String = "" , val answer : String = "" , val isExpanded : Boolean = false
)

data class HelpScreenConfig(
    val versionName : String = "" , val versionCode : Int = 0
)