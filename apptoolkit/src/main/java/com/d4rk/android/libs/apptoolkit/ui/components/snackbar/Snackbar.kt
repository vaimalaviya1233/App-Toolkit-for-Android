package com.d4rk.android.libs.apptoolkit.ui.components.snackbar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * A simple Snackbar composable function.
 *
 * @param message The message to display in the Snackbar.
 * @param showSnackbar A boolean indicating whether the Snackbar should be shown.
 * @param onDismiss A callback function that is invoked when the Snackbar is dismissed.
 */
@Composable
fun Snackbar(
    message : String , showSnackbar : Boolean , onDismiss : () -> Unit
) {
    val snackbarHostState : SnackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = showSnackbar) {
        if (showSnackbar) {
            snackbarHostState.showSnackbar(message = message , duration = SnackbarDuration.Short)
            onDismiss()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        SnackbarHost(
            hostState = snackbarHostState , modifier = Modifier.align(alignment = Alignment.BottomCenter)
        )
    }
}