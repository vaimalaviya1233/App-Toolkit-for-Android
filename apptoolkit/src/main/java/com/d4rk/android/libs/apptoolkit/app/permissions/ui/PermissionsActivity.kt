package com.d4rk.android.libs.apptoolkit.app.permissions.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.d4rk.android.libs.apptoolkit.app.permissions.domain.actions.PermissionsEvent
import com.d4rk.android.libs.apptoolkit.app.theme.style.AppTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class PermissionsActivity : AppCompatActivity() {

    private val viewModel : PermissionsViewModel by viewModel()

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel.onEvent(PermissionsEvent.Load)

        setContent {
            AppTheme {
                PermissionsScreen(viewModel = viewModel)
            }
        }
    }
}