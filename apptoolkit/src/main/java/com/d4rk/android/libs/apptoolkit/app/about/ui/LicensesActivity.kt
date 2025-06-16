package com.d4rk.android.libs.apptoolkit.app.about.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.theme.style.AppTheme
import com.mikepenz.aboutlibraries.ui.compose.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults
import com.mikepenz.aboutlibraries.ui.compose.rememberLibraries

class LicensesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val libraries by rememberLibraries(R.raw.aboutlibraries)
                    LibrariesContainer(
                        libraries = libraries,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 8.dp),
                        padding = LibraryDefaults.libraryPadding(),
                        dimensions = LibraryDefaults.libraryDimensions()
                    )
                }
            }
        }
    }
}
