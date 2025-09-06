package com.d4rk.android.libs.apptoolkit.core.ui.components.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.d4rk.android.libs.apptoolkit.R

/**
 * Simple wrapper around [NativeAdAttribution] that pulls the localized "Ad" text
 * from resources.
 */
@Composable
fun AdLabel(modifier: Modifier = Modifier) {
    NativeAdAttribution(text = stringResource(id = R.string.ad), modifier = modifier)
}
