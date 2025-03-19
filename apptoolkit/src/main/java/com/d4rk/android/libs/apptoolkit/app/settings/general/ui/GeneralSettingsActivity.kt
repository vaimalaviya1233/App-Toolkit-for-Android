package com.d4rk.android.libs.apptoolkit.app.settings.general.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.d4rk.android.libs.apptoolkit.app.display.theme.style.AppTheme
import com.d4rk.android.libs.apptoolkit.app.settings.utils.providers.GeneralSettingsContentProvider
import org.koin.android.ext.android.inject

class GeneralSettingsActivity : AppCompatActivity() {

    private val contentProvider : GeneralSettingsContentProvider by inject()
    private val viewModel : GeneralSettingsViewModel by viewModel()

    companion object {
        private const val EXTRA_TITLE = "extra_title"
        private const val EXTRA_CONTENT = "extra_content"

        fun start(context : Context , title : String , contentKey : String) {
            val intent = Intent(context , GeneralSettingsActivity::class.java).apply {
                putExtra(EXTRA_TITLE , title)
                putExtra(EXTRA_CONTENT , contentKey)
            }

            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val title = intent.getStringExtra(EXTRA_TITLE) ?: getString(com.d4rk.android.libs.apptoolkit.R.string.settings)
        val contentKey = intent.getStringExtra(EXTRA_CONTENT)

        viewModel.loadContent(contentKey)

        setContent {
            AppTheme {
                Surface(modifier = Modifier.fillMaxSize() , color = MaterialTheme.colorScheme.background) {
                    GeneralSettingsScreen(
                        title = title , viewModel = viewModel , contentProvider = contentProvider
                    ) { finish() }
                }
            }
        }
    }
}
