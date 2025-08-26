package com.d4rk.android.libs.apptoolkit.app.ads.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.d4rk.android.libs.apptoolkit.app.settings.utils.providers.BuildInfoProvider
import com.d4rk.android.libs.apptoolkit.app.theme.style.AppTheme
import org.koin.android.ext.android.inject

class AdsSettingsActivity : AppCompatActivity() {
    private val buildInfoProvider : BuildInfoProvider by inject()
    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                AdsSettingsScreen(activity = this@AdsSettingsActivity , buildInfoProvider = buildInfoProvider)
            }
        }
    }
}