package com.d4rk.android.apps.apptoolkit.app.catalog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.d4rk.android.apps.apptoolkit.app.main.ui.theme.AppTheme

class ComponentsCatalogActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Surface(modifier = Modifier) {
                    ComponentsCatalogScreen(paddingValues = PaddingValues())
                }
            }
        }
    }
}

