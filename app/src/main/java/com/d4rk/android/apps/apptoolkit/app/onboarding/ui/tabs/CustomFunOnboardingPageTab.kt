package com.d4rk.android.apps.apptoolkit.app.onboarding.ui.tabs

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.d4rk.android.apps.apptoolkit.app.main.ui.MainActivity
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.SizeConstants

@Composable
fun CustomFunOnboardingPageTab() {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(SizeConstants.ExtraLargeIncreasedSize),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("This is a fully custom tab!")
        Spacer(Modifier.height(12.dp))
        Button(onClick = {
            context.startActivity(Intent(context, MainActivity::class.java))
        }) {
            Text("Let's go!")
        }
    }
}