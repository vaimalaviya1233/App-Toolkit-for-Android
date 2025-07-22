package com.d4rk.android.libs.apptoolkit.core.ui.components.snackbar

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import com.d4rk.android.libs.apptoolkit.core.ui.components.buttons.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.CustomSnackbarVisuals
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.SizeConstants

@Composable
fun DefaultSnackbarHost(snackbarState : SnackbarHostState , modifier : Modifier = Modifier) {
    SnackbarHost(hostState = snackbarState , modifier = modifier) { snackbarData : SnackbarData ->
        (snackbarData.visuals as? CustomSnackbarVisuals)?.let { visuals : CustomSnackbarVisuals ->
            val isError : Boolean = visuals.isError

            Snackbar(
                modifier = Modifier.padding(all = SizeConstants.LargeSize) ,
                containerColor = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.inverseSurface ,
                contentColor = if (isError) MaterialTheme.colorScheme.error else SnackbarDefaults.contentColor ,
                action = {
                    IconButton(
                        onClick = { snackbarData.dismiss() },
                        icon = Icons.Outlined.Close,
                    )
                }) {
                Text(text = visuals.message)
            }
        }
    }
}