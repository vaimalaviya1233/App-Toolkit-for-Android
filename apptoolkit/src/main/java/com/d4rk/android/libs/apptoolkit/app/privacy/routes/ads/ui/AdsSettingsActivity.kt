package com.d4rk.android.libs.apptoolkit.app.privacy.routes.ads.ui

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

class AdsSettingsActivity : AppCompatActivity() {
    private val viewModel: AdsSettingsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AdsSettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
