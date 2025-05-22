package com.d4rk.android.libs.apptoolkit.app.oboarding.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.d4rk.android.libs.apptoolkit.app.settings.general.ui.GeneralSettingsScreen
import com.d4rk.android.libs.apptoolkit.app.theme.style.AppTheme

class OnboardingActivity : ComponentActivity() {

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, OnboardingActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                Surface(modifier = Modifier.fillMaxSize() , color = MaterialTheme.colorScheme.background) {
                    OnboardingScreen()
                }
            }
        }
    }
}
