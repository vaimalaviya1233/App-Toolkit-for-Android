package com.d4rk.android.libs.apptoolkit.core.ui.components.ads

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.d4rk.android.libs.apptoolkit.R

/**
 * Reusable label indicating an advertisement.
 */
@Composable
fun AdLabel(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(id = R.string.ad),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
    )
}
