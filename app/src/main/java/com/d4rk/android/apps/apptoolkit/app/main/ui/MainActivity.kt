package com.d4rk.android.apps.apptoolkit.app.main.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.d4rk.android.apps.apptoolkit.core.data.datastore.DataStore
import com.d4rk.android.libs.apptoolkit.app.display.theme.style.AppTheme
import com.d4rk.android.libs.apptoolkit.app.startup.ui.StartupActivity
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.IntentsHelper
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        println("MainActivity ➡ onCreate()")
        installSplashScreen()
        enableEdgeToEdge()
        MobileAds.initialize(this)
        lifecycleScope.launch {
            val isFirstLaunch = DataStore.getInstance(this@MainActivity).startup.first()
            println("MainActivity ➡ isFirstLaunch = $isFirstLaunch")

            if (isFirstLaunch) {
                println("MainActivity ➡ launching com.d4rk.android.libs.apptoolkit.app.startup.ui.StartupActivity")
                IntentsHelper.openActivity(this@MainActivity , StartupActivity::class.java)
                println("MainActivity ➡ after openActivity(), finishing MainActivity")
                finish()
            }
            else {
                println("MainActivity ➡ rendering MainScreen")
                setContent {
                    AppTheme {
                        Surface(modifier = Modifier.fillMaxSize() , color = MaterialTheme.colorScheme.background) {
                            MainScreen()
                        }
                    }
                }
            }
        }
    }
}