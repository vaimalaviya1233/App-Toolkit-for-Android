package com.d4rk.android.libs.apptoolkit.app.help.domain.repository

import com.d4rk.android.libs.apptoolkit.app.help.domain.data.model.UiHelpQuestion
import kotlinx.coroutines.flow.Flow

interface HelpRepository {
    fun observeFaq(): Flow<List<UiHelpQuestion>>
}
