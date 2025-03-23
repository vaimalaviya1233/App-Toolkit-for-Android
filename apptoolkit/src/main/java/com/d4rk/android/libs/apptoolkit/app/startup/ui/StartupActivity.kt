package com.d4rk.android.libs.apptoolkit.app.startup.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.d4rk.android.libs.apptoolkit.app.display.theme.style.AppTheme
import com.d4rk.android.libs.apptoolkit.app.startup.domain.actions.StartupEvent
import com.d4rk.android.libs.apptoolkit.app.startup.utils.interfaces.providers.StartupProvider
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.IntentsHelper
import com.d4rk.android.libs.apptoolkit.data.datastore.CommonDataStore
import com.google.android.ump.ConsentForm
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class StartupActivity : AppCompatActivity() {
    private val provider : StartupProvider by inject()
    private val viewModel : StartupViewModel by viewModel()
    private lateinit var consentForm : ConsentForm
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppTheme {
                Surface(modifier = Modifier.fillMaxSize() , color = MaterialTheme.colorScheme.background) {
                    StartupScreen(activity = this , viewModel = viewModel)
                }
            }
        }

        if (provider.requiredPermissions.isNotEmpty()) {
            println("StartupActivity ➡ requesting permissions: ${provider.requiredPermissions.joinToString()}")
            permissionLauncher.launch(provider.requiredPermissions)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.screenState.collect { state ->
                    if (state.data?.consentRequired == true && state.data.consentFormLoaded.not()) {
                        viewModel.sendEvent(StartupEvent.OpenConsentForm , this@StartupActivity)
                    }
                }
            }
        }
    }


    fun navigateToNext() {
        println("StartupActivity ➡ navigateToNext() → starting next activity")
        lifecycleScope.launch {
            CommonDataStore.getInstance(this@StartupActivity).saveStartup(isFirstTime = false)

            IntentsHelper.openActivity(context = this@StartupActivity , activityClass = provider.getNextIntent(this@StartupActivity).component?.className?.let {
                Class.forName(it)
            } ?: StartupActivity::class.java)
            finish()
            println("StartupActivity ➡ finished")
        }
    }


}