package com.d4rk.android.libs.apptoolkit.app.settings.settings.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.d4rk.android.libs.apptoolkit.app.display.theme.style.AppTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsActivity : AppCompatActivity() {

    private val viewModel: SettingsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel.loadSettings(this)

        setContent {
            AppTheme {
                Surface(modifier = Modifier.fillMaxSize() , color = MaterialTheme.colorScheme.background) {
                    SettingsScreen(viewModel)
                }
            }
        }
    }
}