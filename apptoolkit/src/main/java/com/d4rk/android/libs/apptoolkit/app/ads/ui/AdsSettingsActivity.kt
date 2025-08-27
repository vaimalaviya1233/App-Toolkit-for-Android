package com.d4rk.android.libs.apptoolkit.app.ads.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.d4rk.android.libs.apptoolkit.app.theme.style.AppTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class AdsSettingsActivity : AppCompatActivity() {
    private val viewModel: AdsSettingsViewModel by viewModel()

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                AdsSettingsScreen(activity = this@AdsSettingsActivity, viewModel = viewModel)
            }
        }
    }
}