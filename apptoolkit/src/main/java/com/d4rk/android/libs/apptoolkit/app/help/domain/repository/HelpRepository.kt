package com.d4rk.android.libs.apptoolkit.app.help.domain.repository

import com.d4rk.android.libs.apptoolkit.app.help.domain.data.model.UiHelpQuestion

interface HelpRepository {
    suspend fun fetchFaq(): List<UiHelpQuestion>
}
