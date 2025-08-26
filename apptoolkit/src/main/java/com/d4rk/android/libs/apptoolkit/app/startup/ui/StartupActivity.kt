package com.d4rk.android.libs.apptoolkit.app.startup.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.d4rk.android.libs.apptoolkit.app.startup.utils.interfaces.providers.StartupProvider
import com.d4rk.android.libs.apptoolkit.app.theme.style.AppTheme
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.ConsentFormHelper
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.IntentsHelper
import com.d4rk.android.libs.apptoolkit.app.main.utils.InAppUpdateHelper
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.ump.ConsentInformation
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class StartupActivity : AppCompatActivity() {
    private val provider : StartupProvider by inject()
    var consentFormLoaded : Boolean = false
    private val permissionLauncher : ActivityResultLauncher<Array<String>> = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }
    private var updateResultLauncher: ActivityResultLauncher<IntentSenderRequest> =
        registerForActivityResult(contract = ActivityResultContracts.StartIntentSenderForResult()) {}
    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            if (provider.requiredPermissions.isNotEmpty()) {
                permissionLauncher.launch(input = provider.requiredPermissions)
            }
            checkForUpdates()
            checkUserConsent()
        }
    }

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lifecycle.addObserver(lifecycleObserver)

        setContent {
            AppTheme {
                Surface(modifier = Modifier.fillMaxSize() , color = MaterialTheme.colorScheme.background) {
                    StartupScreen(activity = this)
                }
            }
        }
    }

    fun navigateToNext() {
        lifecycleScope.launch {
            IntentsHelper.openActivity(context = this@StartupActivity , activityClass = provider.getNextIntent(this@StartupActivity).component?.className?.let {
                Class.forName(it)
            } ?: StartupActivity::class.java)
            finish()
        }
    }

    private fun checkUserConsent() {
        lifecycleScope.launch {
            val consentInfo: ConsentInformation = UserMessagingPlatform.getConsentInformation(this@StartupActivity)
            ConsentFormHelper.showConsentFormIfRequired(activity = this@StartupActivity , consentInfo = consentInfo)
            consentFormLoaded = true
        }
    }

    private fun checkForUpdates() {
        lifecycleScope.launch {
            InAppUpdateHelper.performUpdate(
                appUpdateManager = AppUpdateManagerFactory.create(this@StartupActivity),
                updateResultLauncher = updateResultLauncher,
            )
        }
    }
}