package com.d4rk.android.libs.apptoolkit.ui.components.spacers

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ButtonIconSpacer() {
    Spacer(modifier = Modifier.size(size = ButtonDefaults.IconSpacing))
}