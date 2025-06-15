package com.d4rk.android.apps.apptoolkit.app.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.d4rk.android.libs.apptoolkit.core.ui.components.spacers.LargeVerticalSpacer
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.SizeConstants
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.ClipboardHelper

private data class ComponentDemo(
    val title: String,
    val demo: @Composable () -> Unit,
    val code: String
)

@Composable
fun ComponentsCatalogScreen(paddingValues: PaddingValues) {
    val context = LocalContext.current
    val components = listOf(
        ComponentDemo(
            title = "LargeVerticalSpacer",
            demo = { LargeVerticalSpacer() },
            code = "LargeVerticalSpacer()"
        ),
        ComponentDemo(
            title = "LargeHorizontalSpacer",
            demo = { com.d4rk.android.libs.apptoolkit.core.ui.components.spacers.LargeHorizontalSpacer() },
            code = "LargeHorizontalSpacer()"
        )
    )

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(paddingValues)
            .padding(SizeConstants.LargeSize),
        verticalArrangement = Arrangement.spacedBy(SizeConstants.LargeSize)
    ) {
        components.forEach { item ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(SizeConstants.LargeSize)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    LargeVerticalSpacer()
                    item.demo()
                    LargeVerticalSpacer()
                    Text(
                        text = item.code,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                    LargeVerticalSpacer()
                    Button(onClick = {
                        ClipboardHelper.copyTextToClipboard(
                            context = context,
                            label = item.title,
                            text = item.code
                        )
                    }) {
                        Text(text = "Copy")
                    }
                }
            }
        }
    }
}

