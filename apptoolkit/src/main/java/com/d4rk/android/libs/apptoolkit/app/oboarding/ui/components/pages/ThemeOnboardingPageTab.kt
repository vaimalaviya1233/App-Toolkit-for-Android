package com.d4rk.android.libs.apptoolkit.app.oboarding.ui.components.pages

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Tonality
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.core.ui.components.modifiers.bounceClick
import com.d4rk.android.libs.apptoolkit.core.ui.components.spacers.ExtraExtraLargeVerticalSpacer
import com.d4rk.android.libs.apptoolkit.core.ui.components.spacers.LargeHorizontalSpacer
import com.d4rk.android.libs.apptoolkit.core.ui.components.spacers.LargeVerticalSpacer
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.SizeConstants
import com.d4rk.android.libs.apptoolkit.data.datastore.CommonDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

data class OnboardingThemeChoice(
    val name : String , val icon : ImageVector , val description : String
)

@Composable
fun ThemeOnboardingPageTab() {
    val coroutineScope : CoroutineScope = rememberCoroutineScope()
    val context: Context = LocalContext.current
    val dataStore: CommonDataStore = CommonDataStore.getInstance(context = context)

    val defaultThemeModeName : String = stringResource(id = R.string.follow_system)
    val lightModeName : String = stringResource(id = R.string.light_mode)
    val darkModeName : String = stringResource(id = R.string.dark_mode)

    val currentThemeMode : String = dataStore.themeMode.collectAsState(initial = defaultThemeModeName).value
    val isAmoledMode : State<Boolean> = dataStore.amoledMode.collectAsState(initial = false)

    val themeChoices : List<OnboardingThemeChoice> = listOf(
        OnboardingThemeChoice(
            name = lightModeName , icon = Icons.Filled.LightMode , description = stringResource(R.string.onboarding_theme_light_desc)
        ) , OnboardingThemeChoice(
            name = darkModeName , icon = Icons.Filled.DarkMode , description = stringResource(R.string.onboarding_theme_dark_desc)
        ) , OnboardingThemeChoice(
            name = defaultThemeModeName , icon = Icons.Filled.BrightnessAuto , description = stringResource(R.string.onboarding_theme_system_desc)
        )
    )

    Column(
        modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SizeConstants.LargeSize) ,
        horizontalAlignment = Alignment.CenterHorizontally , verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.onboarding_theme_title) , style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold , fontSize = 30.sp , textAlign = TextAlign.Center
            ) , color = MaterialTheme.colorScheme.onSurface
        )

        LargeVerticalSpacer()

        Text(
            text = stringResource(R.string.onboarding_theme_subtitle) , style = MaterialTheme.typography.bodyLarge , textAlign = TextAlign.Center , color = MaterialTheme.colorScheme.onSurfaceVariant , modifier = Modifier.padding(horizontal = SizeConstants.LargeSize)
        )

        themeChoices.forEachIndexed { index , choice ->
            ThemeChoiceCard(
                choice = choice , isSelected = currentThemeMode == choice.name , onClick = {
                    coroutineScope.launch {
                        dataStore.saveThemeMode(mode = choice.name)

                    }
                })
            if (index < themeChoices.lastIndex) {
                LargeVerticalSpacer()
            }
        }

        ExtraExtraLargeVerticalSpacer()

        AmoledModeToggle(
            isAmoledMode = isAmoledMode.value , onCheckedChange = { isChecked ->
                coroutineScope.launch {
                    dataStore.saveAmoledMode(isChecked = isChecked)
                }
            })

        ExtraExtraLargeVerticalSpacer()
    }
}

@Composable
fun ThemeChoiceCard(
    choice : OnboardingThemeChoice , isSelected : Boolean , onClick : () -> Unit
) {
    val cardColors = if (isSelected) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer , contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
    else {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant , contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    val border = if (isSelected) {
        BorderStroke(SizeConstants.ExtraTinySize , MaterialTheme.colorScheme.primary)
    }
    else {
        BorderStroke(1.dp , MaterialTheme.colorScheme.outlineVariant)
    }

    Card(
        modifier = Modifier
                .fillMaxWidth()
                .bounceClick()
                .clickable(onClick = onClick) , shape = RoundedCornerShape(16.dp) , colors = cardColors , elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp) , border = border
    ) {
        Row(
            modifier = Modifier
                    .padding(horizontal = 24.dp , vertical = SizeConstants.LargeIncreasedSize)
                    .fillMaxWidth() , verticalAlignment = Alignment.CenterVertically , horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = choice.icon , contentDescription = choice.name , modifier = Modifier.size(SizeConstants.ExtraLargeIncreasedSize)
                )
                LargeHorizontalSpacer()
                Column {
                    Text(
                        text = choice.name ,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold) ,
                    )
                    Text(
                        text = choice.description , style = MaterialTheme.typography.bodyMedium , color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
            AnimatedVisibility(
                visible = isSelected , enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn() , exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                            .size(SizeConstants.ExtraLargeSize)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary) , contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Tonality , contentDescription = stringResource(R.string.selected) , tint = MaterialTheme.colorScheme.onPrimary , modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AmoledModeToggle(
    isAmoledMode : Boolean , onCheckedChange : (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth() , shape = RoundedCornerShape(16.dp) , color = MaterialTheme.colorScheme.surfaceContainerHighest , tonalElevation = 3.dp , shadowElevation = 3.dp
    ) {
        Row(modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(! isAmoledMode) }
                .padding(horizontal = 24.dp , vertical = SizeConstants.LargeIncreasedSize) , verticalAlignment = Alignment.CenterVertically , horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.amoled_mode) , style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold) , color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(id = R.string.onboarding_amoled_mode_desc) , style = MaterialTheme.typography.bodySmall , color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            LargeHorizontalSpacer()
            Switch(checked = isAmoledMode , onCheckedChange = onCheckedChange , thumbContent = {
                Icon(
                    imageVector = Icons.Filled.Tonality , contentDescription = null , modifier = Modifier.size(SizeConstants.SwitchIconSize) , tint = if (isAmoledMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            } , colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary , checkedTrackColor = MaterialTheme.colorScheme.primaryContainer , uncheckedThumbColor = MaterialTheme.colorScheme.outline , uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            ))
        }
    }
}