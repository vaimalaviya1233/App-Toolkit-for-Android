package com.d4rk.android.libs.apptoolkit.app.oboarding.ui.components.pages

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.core.ui.components.modifiers.bounceClick
import com.d4rk.android.libs.apptoolkit.core.ui.components.spacers.LargeHorizontalSpacer
import com.d4rk.android.libs.apptoolkit.core.ui.components.spacers.LargeVerticalSpacer
import com.d4rk.android.libs.apptoolkit.core.utils.constants.links.AppLinks
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.SizeConstants
import com.d4rk.android.libs.apptoolkit.data.datastore.CommonDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun CrashlyticsOnboardingPageTab() {
    val context : Context = LocalContext.current
    val dataStore : CommonDataStore = CommonDataStore.getInstance(context = context)
    val coroutineScope : CoroutineScope = rememberCoroutineScope()

    val initialSwitchState = true
    val switchState : State<Boolean> = dataStore.usageAndDiagnostics(default = initialSwitchState).collectAsState(initial = initialSwitchState)

    Column(
        modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp , vertical = SizeConstants.ExtraLargeIncreasedSize) , horizontalAlignment = Alignment.CenterHorizontally , verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.Analytics , contentDescription = null , modifier = Modifier.size(64.dp) , tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.onboarding_crashlytics_title) , style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold , fontSize = 30.sp , textAlign = TextAlign.Center
                ) , color = MaterialTheme.colorScheme.onSurface
            )

            LargeVerticalSpacer()

            Text(
                text = stringResource(R.string.onboarding_crashlytics_description) ,

                style = MaterialTheme.typography.bodyLarge , textAlign = TextAlign.Center , color = MaterialTheme.colorScheme.onSurfaceVariant , modifier = Modifier.padding(horizontal = SizeConstants.LargeSize)
            )

            Spacer(modifier = Modifier.height(40.dp))

            UsageAndDiagnosticsToggleCard(
                switchState = switchState.value , onCheckedChange = { isChecked ->
                    coroutineScope.launch {
                        dataStore.saveUsageAndDiagnostics(isChecked = isChecked)
                    }
                })

            Spacer(modifier = Modifier.height(24.dp))

            LearnMoreSection(context)
        }

    }
}

@Composable
fun UsageAndDiagnosticsToggleCard(
    switchState : Boolean , onCheckedChange : (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth() , shape = MaterialTheme.shapes.large , colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ) , elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(! switchState) }
                .padding(horizontal = SizeConstants.LargeIncreasedSize , vertical = 16.dp) , verticalAlignment = Alignment.CenterVertically , horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.usage_and_diagnostics) , style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold) , color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    modifier = Modifier.animateContentSize() , text = if (switchState) stringResource(R.string.onboarding_crashlytics_enabled_desc)
                    else stringResource(R.string.onboarding_crashlytics_disabled_desc) , style = MaterialTheme.typography.bodySmall , color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            LargeHorizontalSpacer()
            Switch(checked = switchState , onCheckedChange = onCheckedChange , thumbContent = {
                AnimatedContent(targetState = switchState , transitionSpec = {
                    if (targetState) {
                        slideInVertically { height -> height } + fadeIn() togetherWith slideOutVertically { height -> - height } + fadeOut()
                    }
                    else {
                        slideInVertically { height -> - height } + fadeIn() togetherWith slideOutVertically { height -> height } + fadeOut()
                    } using SizeTransform(clip = false)
                } , label = "SwitchIconAnimation") { targetChecked ->
                    Icon(
                        imageVector = if (targetChecked) Icons.Filled.Analytics else Icons.Filled.Policy ,
                        contentDescription = null ,
                        modifier = Modifier.size(SizeConstants.SwitchIconSize) ,
                        tint = if (targetChecked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } , colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary , checkedTrackColor = MaterialTheme.colorScheme.primaryContainer , uncheckedThumbColor = MaterialTheme.colorScheme.outline , uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            ))
        }
    }
}

@Composable
fun LearnMoreSection(context : Context) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp) , thickness = 0.5.dp)
        Text(
            text = stringResource(R.string.onboarding_crashlytics_privacy_info) ,

            style = MaterialTheme.typography.bodySmall , textAlign = TextAlign.Center , color = MaterialTheme.colorScheme.onSurfaceVariant , modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = {
            val intent = Intent(Intent.ACTION_VIEW , AppLinks.PRIVACY_POLICY.toUri())
            context.startActivity(intent)
        } , modifier = Modifier.bounceClick() , colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)) {
            Icon(
                Icons.AutoMirrored.Filled.Launch , contentDescription = null , modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
            Text(
                text = stringResource(id = R.string.learn_more_privacy_policy) , textDecoration = TextDecoration.Underline
            )
        }
    }
}