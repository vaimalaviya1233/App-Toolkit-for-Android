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
import androidx.lifecycle.lifecycleScope
import com.d4rk.android.libs.apptoolkit.app.startup.domain.actions.StartupEvent
import com.d4rk.android.libs.apptoolkit.app.startup.utils.interfaces.providers.StartupProvider
import com.d4rk.android.libs.apptoolkit.app.theme.style.AppTheme
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.IntentsHelper
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class StartupActivity : AppCompatActivity() {
    private val provider : StartupProvider by inject()
    private val viewModel : StartupViewModel by viewModel()
    private val permissionLauncher : ActivityResultLauncher<Array<String>> = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

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
    }

    override fun onResume() {
        super.onResume()
        if (provider.requiredPermissions.isNotEmpty()) {
            permissionLauncher.launch(input = provider.requiredPermissions)
        }

        viewModel.onEvent(event = StartupEvent.OpenConsentForm(activity = this@StartupActivity))
    }

    fun navigateToNext() {
        lifecycleScope.launch {
            IntentsHelper.openActivity(context = this@StartupActivity , activityClass = provider.getNextIntent(this@StartupActivity).component?.className?.let {
                Class.forName(it)
            } ?: StartupActivity::class.java)
            finish()
        }
    }
}