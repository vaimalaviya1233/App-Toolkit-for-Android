package com.d4rk.android.libs.apptoolkit.app.startup.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.d4rk.android.libs.apptoolkit.app.startup.utils.interfaces.providers.StartupProvider
import com.d4rk.android.libs.apptoolkit.app.theme.style.AppTheme
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.ConsentFormHelper
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.IntentsHelper
import com.google.android.ump.ConsentInformation
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class StartupActivity : AppCompatActivity() {
    private val provider: StartupProvider by inject()
    private val _consentFormLoaded = MutableStateFlow(false)
    val consentFormLoaded: StateFlow<Boolean> = _consentFormLoaded.asStateFlow()
    private val permissionLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppTheme {
                val consentFormLoadedState by consentFormLoaded.collectAsStateWithLifecycle()
                StartupScreen(
                    consentFormLoaded = consentFormLoadedState,
                    onContinueClick = { navigateToNext() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (provider.requiredPermissions.isNotEmpty()) {
            permissionLauncher.launch(input = provider.requiredPermissions)
        }

        checkUserConsent()
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
            val consentInfo: ConsentInformation =
                UserMessagingPlatform.getConsentInformation(this@StartupActivity)
            ConsentFormHelper.showConsentFormIfRequired(
                activity = this@StartupActivity,
                consentInfo = consentInfo
            )
            _consentFormLoaded.value = true
        }
    }
}