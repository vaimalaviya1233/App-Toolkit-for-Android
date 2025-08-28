package com.d4rk.android.libs.apptoolkit.app.startup.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.d4rk.android.libs.apptoolkit.app.startup.domain.actions.StartupAction
import com.d4rk.android.libs.apptoolkit.app.startup.domain.actions.StartupEvent
import com.d4rk.android.libs.apptoolkit.app.startup.utils.interfaces.providers.StartupProvider
import com.d4rk.android.libs.apptoolkit.app.theme.style.AppTheme
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.ConsentFormHelper
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.IntentsHelper
import com.google.android.ump.ConsentInformation
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class StartupActivity : AppCompatActivity() {
    private val provider : StartupProvider by inject()
    private val viewModel : StartupViewModel by viewModel()
    private val permissionLauncher : ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.actionEvent.collect { action : StartupAction ->
                    when (action) {
                        StartupAction.NavigateNext -> navigateToNext()
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                if (provider.requiredPermissions.isNotEmpty()) {
                    permissionLauncher.launch(provider.requiredPermissions)
                }
                checkUserConsent()
            }
        }

        setContent {
            AppTheme {
                val screenState by viewModel.uiState.collectAsStateWithLifecycle()
                StartupScreen(
                    screenState = screenState,
                    onContinueClick = { viewModel.onEvent(StartupEvent.Continue) }
                )
            }
        }
    }

    private fun navigateToNext() {
        IntentsHelper.openActivity(
            context = this@StartupActivity,
            activityClass = provider.getNextIntent(this@StartupActivity)
                .component?.className?.let { Class.forName(it) }
                ?: StartupActivity::class.java
        )
        finish()
    }

    private suspend fun checkUserConsent() {
        val consentInfo: ConsentInformation =
            UserMessagingPlatform.getConsentInformation(this)
        ConsentFormHelper.showConsentFormIfRequired(
            activity = this,
            consentInfo = consentInfo,
        )
        viewModel.onEvent(StartupEvent.ConsentFormLoaded)
    }
}
