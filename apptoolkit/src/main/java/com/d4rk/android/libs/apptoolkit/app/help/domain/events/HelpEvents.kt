package com.d4rk.android.libs.apptoolkit.app.help.domain.events

import com.d4rk.android.libs.apptoolkit.core.ui.base.handling.UiEvent

sealed interface HelpEvents : UiEvent {
    data object LoadHelp : HelpEvents
}
