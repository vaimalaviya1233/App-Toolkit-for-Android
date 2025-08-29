package com.d4rk.android.libs.apptoolkit.app.help.domain.model.ui

import com.d4rk.android.libs.apptoolkit.app.help.domain.data.model.UiHelpQuestion

data class UiHelpScreen(
    val questions: List<UiHelpQuestion> = emptyList()
)

