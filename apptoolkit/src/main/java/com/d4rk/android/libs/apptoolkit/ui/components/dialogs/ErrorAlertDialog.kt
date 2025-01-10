package com.d4rk.android.libs.apptoolkit.ui.components.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/**
 * Displays an error dialog with a given error message.
 *
 * @param errorMessage The message to be displayed in the error dialog.
 * @param onDismiss A callback function to be invoked when the dialog is dismissed (either by clicking the confirm button or tapping outside the dialog).
 */
@Composable
fun ErrorAlertDialog(
    errorMessage : String , onDismiss : () -> Unit
) {
    AlertDialog(onDismissRequest = onDismiss ,
                title = { Text(text = "Error") } ,
                text = { Text(text = errorMessage) } ,
                confirmButton = {
                    TextButton(onClick = onDismiss) {
                        Text(text = stringResource(id = android.R.string.ok))
                    }
                })
}