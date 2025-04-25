package com.d4rk.android.libs.apptoolkit.core.ui.components.dialogs

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(onDateSelected : (String) -> Unit , onDismiss : () -> Unit) {
    val selectedDatePickerState : DatePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())

    androidx.compose.material3.DatePickerDialog(onDismissRequest = {
        onDismiss()
    } , confirmButton = {
        TextButton(onClick = {
            onDismiss()
            selectedDatePickerState.selectedDateMillis?.let {
                onDateSelected(SimpleDateFormat("yyyy-MM-dd" , Locale.getDefault()).format(Date(it)))
            }
        }) {
            Text(text = stringResource(id = android.R.string.ok))
        }
    } , dismissButton = {
        TextButton(onClick = {
            onDismiss()
        }) {
            Text(text = stringResource(id = android.R.string.cancel))
        }
    }) {
        DatePicker(state = selectedDatePickerState)
    }
}