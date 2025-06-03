package com.d4rk.android.libs.apptoolkit.app.diagnostics.ui

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.settings.utils.providers.BuildInfoProvider
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.sections.InfoMessageSection
import com.d4rk.android.libs.apptoolkit.core.ui.components.preferences.SwitchCardItem
import com.d4rk.android.libs.apptoolkit.core.ui.components.spacers.LargeHorizontalSpacer
import com.d4rk.android.libs.apptoolkit.core.ui.components.spacers.SmallVerticalSpacer
import com.d4rk.android.libs.apptoolkit.core.utils.constants.links.AppLinks
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.SizeConstants
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.ConsentManagerHelper
import com.d4rk.android.libs.apptoolkit.data.datastore.CommonDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun UsageAndDiagnosticsList(paddingValues : PaddingValues , configProvider : BuildInfoProvider) {
    val context : Context = LocalContext.current
    val dataStore : CommonDataStore = CommonDataStore.getInstance(context = context)
    val coroutineScope : CoroutineScope = rememberCoroutineScope()
    val switchState : State<Boolean> = dataStore.usageAndDiagnostics(default = ! configProvider.isDebugBuild).collectAsState(initial = ! configProvider.isDebugBuild)

    // Collect states for each consent type
    val analyticsState = dataStore.analyticsConsent.collectAsState(initial = false)
    val adStorageState = dataStore.adStorageConsent.collectAsState(initial = false)
    val adUserDataState = dataStore.adUserDataConsent.collectAsState(initial = false)
    val adPersonalizationState = dataStore.adPersonalizationConsent.collectAsState(initial = false)

    var advancedSettingsExpanded by remember { mutableStateOf(false) }

    // Helper function to update all consents
    fun updateAllConsents() {
        ConsentManagerHelper.updateConsent(
            analyticsGranted = analyticsState.value , adStorageGranted = adStorageState.value , adUserDataGranted = adUserDataState.value , adPersonalizationGranted = adPersonalizationState.value
        )
    }

    LazyColumn(contentPadding = paddingValues , modifier = Modifier.fillMaxSize()) {
        item {
            SwitchCardItem(title = stringResource(id = R.string.usage_and_diagnostics) , switchState = switchState) { isChecked : Boolean ->
                coroutineScope.launch {
                    dataStore.saveUsageAndDiagnostics(isChecked = isChecked)
                    // ConsentManagerHelper.updateConsent(usageAndDiagnosticsEnabled = isChecked)
                }
            }
        }

        // --- Expandable Advanced Privacy Settings ---
        item {
            ExpandableConsentSectionHeader(
                title = stringResource(R.string.advanced_privacy_settings) , expanded = advancedSettingsExpanded , onToggle = { advancedSettingsExpanded = ! advancedSettingsExpanded })
        }

        // --- Privacy Consent Items (conditionally visible) ---
        item {
            AnimatedVisibility(
                visible = advancedSettingsExpanded ,
                enter = expandVertically(expandFrom = Alignment.Top) ,
                exit = shrinkVertically(shrinkTowards = Alignment.Top)
            ) {
                Column(modifier = Modifier.padding(horizontal = SizeConstants.SmallSize)) { // 8.dp
                    ConsentSectionHeader(title = stringResource(R.string.consent_category_analytics_title))
                    ConsentToggleCard(
                        title = stringResource(R.string.consent_analytics_storage_title) ,
                        descriptionOn = stringResource(R.string.consent_analytics_storage_description_on) ,
                        descriptionOff = stringResource(R.string.consent_analytics_storage_description_off) ,
                        detailedExplanation = stringResource(R.string.consent_category_analytics_description) ,
                        switchState = analyticsState.value ,
                        icon = Icons.Default.Analytics ,
                        onCheckedChange = { isChecked ->
                            coroutineScope.launch {
                                dataStore.saveAnalyticsConsent(isGranted = isChecked)
                                updateAllConsents()
                            }
                        })
                    SmallVerticalSpacer() // 8.dp

                    ConsentSectionHeader(title = stringResource(R.string.consent_category_advertising_title))
                    ConsentToggleCard(
                        title = stringResource(R.string.consent_ad_storage_title) ,
                        descriptionOn = stringResource(R.string.consent_ad_storage_description_on) ,
                        descriptionOff = stringResource(R.string.consent_ad_storage_description_off) ,
                        detailedExplanation = stringResource(R.string.consent_category_advertising_description) ,
                        switchState = adStorageState.value ,
                        icon = Icons.Default.Storage ,
                        onCheckedChange = { isChecked ->
                            coroutineScope.launch {
                                dataStore.saveAdStorageConsent(isGranted = isChecked)
                                updateAllConsents()
                            }
                        })
                    SmallVerticalSpacer() // 8.dp

                    ConsentToggleCard(
                        title = stringResource(R.string.consent_ad_user_data_title) ,
                        descriptionOn = stringResource(R.string.consent_ad_user_data_description_on) ,
                        descriptionOff = stringResource(R.string.consent_ad_user_data_description_off) ,
                        detailedExplanation = stringResource(R.string.consent_ad_user_data_detailed_explanation) ,
                        switchState = adUserDataState.value ,
                        icon = Icons.AutoMirrored.Filled.Send ,
                        onCheckedChange = { isChecked ->
                            coroutineScope.launch {
                                dataStore.saveAdUserDataConsent(isGranted = isChecked)
                                updateAllConsents()
                            }
                        })
                    SmallVerticalSpacer() // 8.dp

                    ConsentToggleCard(
                        title = stringResource(R.string.consent_ad_personalization_title) ,
                        descriptionOn = stringResource(R.string.consent_ad_personalization_description_on) ,
                        descriptionOff = stringResource(R.string.consent_ad_personalization_description_off) ,
                        detailedExplanation = stringResource(R.string.consent_ad_personalization_detailed_explanation) ,
                        switchState = adPersonalizationState.value ,
                        icon = Icons.Default.Campaign ,
                        onCheckedChange = { isChecked ->
                            coroutineScope.launch {
                                dataStore.saveAdPersonalizationConsent(isGranted = isChecked)
                                updateAllConsents()
                            }
                        })
                }
            }
        }

        item {
            InfoMessageSection(
                modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 24.dp) , // Consider if 24.dp should be a SizeConstant e.g. ExtraLargeSize or a new one
                message = stringResource(id = R.string.summary_usage_and_diagnostics) , learnMoreText = stringResource(id = R.string.learn_more) , learnMoreUrl = AppLinks.PRIVACY_POLICY
            )
        }
    }
}

@Composable
fun ExpandableConsentSectionHeader(
    title : String , expanded : Boolean , onToggle : () -> Unit
) {
    Row(
        modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(
                    horizontal = SizeConstants.LargeSize , // 16.dp
                    vertical = SizeConstants.MediumSize   // 12.dp
                ) , verticalAlignment = Alignment.CenterVertically , horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title , style = MaterialTheme.typography.titleLarge , fontWeight = FontWeight.Bold , color = MaterialTheme.colorScheme.primary
        )
        IconButton(onClick = onToggle) {
            Icon(
                imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore , contentDescription = if (expanded) stringResource(R.string.icon_desc_expand_less) else stringResource(R.string.icon_desc_expand_more) , tint = MaterialTheme.colorScheme.primary
                // Icon size here is default from IconButton, usually 24.dp.
                // If you need to customize, use Modifier.size()
            )
        }
    }
}


@Composable
fun ConsentSectionHeader(title : String) {
    Text(
        text = title , style = MaterialTheme.typography.titleMedium , fontWeight = FontWeight.Bold , color = MaterialTheme.colorScheme.primary , modifier = Modifier.padding(
            top = SizeConstants.LargeSize ,    // 16.dp
            bottom = SizeConstants.SmallSize ,   // 8.dp
            start = SizeConstants.SmallSize ,    // 8.dp
            end = SizeConstants.SmallSize     // 8.dp
        )
    )
}

@Composable
fun ConsentToggleCard(
    title : String , descriptionOn : String , descriptionOff : String , detailedExplanation : String , switchState : Boolean , icon : ImageVector , onCheckedChange : (Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = SizeConstants.ExtraSmallSize) , // 4.dp
        shape = MaterialTheme.shapes.large , colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ) , elevation = CardDefaults.cardElevation(defaultElevation = 1.dp) // 1.dp is fine, or create TinySize if used often
    ) {
        Column {
            Row(modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCheckedChange(! switchState) }
                    .padding(
                        horizontal = SizeConstants.LargeSize , // 16.dp
                        vertical = SizeConstants.MediumSize   // 12.dp
                    ) , verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon ,
                    contentDescription = stringResource(R.string.icon_desc_consent_category) ,
                    tint = MaterialTheme.colorScheme.primary ,
                    modifier = Modifier.size(24.dp) // Consider if this should be a SizeConstant e.g. LargeIncreasedSize (20dp) or ExtraLargeSize (28dp) or a new one for 24dp
                )
                LargeHorizontalSpacer() // 16.dp
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title , style = MaterialTheme.typography.titleMedium , fontWeight = FontWeight.SemiBold , color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        modifier = Modifier.animateContentSize() , text = if (switchState) descriptionOn else descriptionOff , style = MaterialTheme.typography.bodySmall , color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
                LargeHorizontalSpacer() // 16.dp
                Switch(
                    checked = switchState , onCheckedChange = onCheckedChange , thumbContent = {
                        Icon(
                            imageVector = if (switchState) Icons.Filled.Check else Icons.Filled.Close , contentDescription = stringResource(R.string.icon_desc_switch_status) , modifier = Modifier.size(SizeConstants.SwitchIconSize) , // Use constant
                            tint = if (switchState) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } , colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary , checkedTrackColor = MaterialTheme.colorScheme.primaryContainer , uncheckedThumbColor = MaterialTheme.colorScheme.outline , uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                )
            }
            // Detailed Explanation (Expandable)
            Row(Modifier
                        .fillMaxWidth()
                        .clickable { expanded = ! expanded }
                        .padding(
                            start = SizeConstants.LargeSize ,    // 16.dp
                            end = SizeConstants.LargeSize ,      // 16.dp
                            bottom = if (expanded) SizeConstants.MediumSize else 0.dp , // 12.dp or 0.dp
                            top = if (expanded) SizeConstants.SmallSize else 0.dp     // 8.dp or 0.dp
                        ) , verticalAlignment = Alignment.CenterVertically) {
                if (expanded) {
                    Text(
                        text = detailedExplanation ,
                        style = MaterialTheme.typography.bodySmall ,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) ,
                    )
                }
                else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(R.string.learn_more) , style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary) , modifier = Modifier.padding(vertical = SizeConstants.ExtraSmallSize) // 4.dp
                        )
                    }
                }
            }
        }
    }
}