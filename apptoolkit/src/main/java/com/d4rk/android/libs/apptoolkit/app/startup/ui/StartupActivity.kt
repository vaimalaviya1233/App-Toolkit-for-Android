package com.d4rk.android.libs.apptoolkit.app.startup.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.d4rk.android.libs.apptoolkit.app.display.theme.style.AppTheme
import com.d4rk.android.libs.apptoolkit.app.settings.settings.ui.SettingsScreen
import com.d4rk.android.libs.apptoolkit.app.startup.utils.interfaces.providers.StartupProvider
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.android.ext.android.inject

class StartupActivity : AppCompatActivity() {

    val provider : StartupProvider by inject()
    private lateinit var consentForm : ConsentForm
    val consentShown : MutableStateFlow<Boolean> = MutableStateFlow(value = false)

    private val permissionLauncher : ActivityResultLauncher<Array<String>> = registerForActivityResult(contract = ActivityResultContracts.RequestMultiplePermissions()) {}

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                Surface(modifier = Modifier.fillMaxSize() , color = MaterialTheme.colorScheme.background) {
                    StartupScreen(activity = this)
                }
            }
        }

        if (provider.requiredPermissions.isNotEmpty()) {
            permissionLauncher.launch(provider.requiredPermissions)
        }

        provider.consentRequestParameters?.let { params ->
            val info = UserMessagingPlatform.getConsentInformation(this)
            info.requestConsentInfoUpdate(this , params , { if (info.isConsentFormAvailable) loadConsentForm() } , { navigateToNext() })
        } ?: navigateToNext()
    }

    private fun loadConsentForm() {
        UserMessagingPlatform.loadConsentForm(this , { form ->
            consentForm = form
            if (UserMessagingPlatform.getConsentInformation(this).consentStatus == ConsentInformation.ConsentStatus.REQUIRED) {
                consentShown.value = true
                consentForm.show(this) { loadConsentForm() }
            }
            else navigateToNext()
        } , { navigateToNext() })
    }

    private fun navigateToNext() {
        startActivity(provider.getNextIntent(this))
        finish()
    }
}
