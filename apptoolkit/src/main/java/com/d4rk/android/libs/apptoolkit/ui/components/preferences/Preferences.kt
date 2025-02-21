package com.d4rk.android.libs.apptoolkit.ui.components.preferences

import android.view.SoundEffectConstants
import android.view.View
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.d4rk.android.libs.apptoolkit.ui.components.spacers.LargeHorizontalSpacer
import com.d4rk.android.libs.apptoolkit.utils.constants.ui.SizeConstants

/**
 * Creates a clickable card with a title and a switch for app preference screens.
 *
 * This composable function displays a card with a title and a switch. The entire card is clickable, and clicking it toggles the switch and invokes the `onSwitchToggled` callback.
 * The switch visually indicates its 'on' state by displaying a check icon within the thumb.
 *
 * @param title The text displayed as the card's title.
 * @param switchState A [State] object holding the current on/off state of the switch. Use `true` for the 'on' state and `false` for the 'off' state.
 * @param onSwitchToggled A callback function invoked when the switch is toggled, either by clicking the card or the switch itself.  It receives the new state of the switch (a `Boolean` value) as a parameter.
 *
 * The card has a rounded corner shape and provides a click sound effect upon interaction.
 */
@Composable
fun SwitchCardComposable(
    title : String , switchState : State<Boolean> , onSwitchToggled : (Boolean) -> Unit
) {
    val view : View = LocalView.current
    Card(shape = RoundedCornerShape(size = SizeConstants.ExtraLargeSize) , modifier = Modifier
            .fillMaxWidth()
            .padding(all = 24.dp)
            .clip(shape = RoundedCornerShape(size = SizeConstants.ExtraLargeSize))
            .clickable {
                view.playSoundEffect(SoundEffectConstants.CLICK)
                onSwitchToggled(! switchState.value)
            }) {
        Row(
            modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = SizeConstants.LargeSize) , horizontalArrangement = Arrangement.SpaceBetween , verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title)
            Switch(checked = switchState.value , onCheckedChange = { isChecked ->
                onSwitchToggled(isChecked)
            } , thumbContent = if (switchState.value) {
                {
                    Icon(
                        Icons.Filled.Check ,
                        contentDescription = null ,
                        modifier = Modifier.size(SwitchDefaults.IconSize) ,
                    )
                }
            }
            else {
                null
            })
        }
    }
}

/**
 * A composable function that creates a settings preference item card.
 *
 * This function wraps a [PreferenceItem] composable inside a [Card] to provide a visually
 * distinct and interactive element for settings screens. It allows customization of the icon,
 * title, summary, ripple effect, and the action to perform when clicked.
 *
 * @param icon The optional [ImageVector] to display as an icon in the preference item.
 *             If null, no icon will be displayed.
 * @param title The optional [String] to display as the title of the preference item.
 *              If null, no title will be displayed.
 * @param summary The optional [String] to display as the summary of the preference item.
 *               If null, no summary will be displayed.
 * @param rippleEffectDp The [Dp] value to control the size of the ripple effect when the item is clicked.
 *                      Defaults to 2.dp.
 * @param onClick The lambda function to execute when the preference item is clicked.
 *                Defaults to an empty lambda, meaning no action will be performed by default.
 */
@Composable
fun SettingsPreferenceItem(
    icon : ImageVector? = null , title : String? = null , summary : String? = null , rippleEffectDp : Dp = 2.dp , onClick : () -> Unit = {}
) {
    Card(
        modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(size = 2.dp)) ,
        shape = RoundedCornerShape(size = 2.dp) ,
    ) {
        PreferenceItem(rippleEffectDp = rippleEffectDp , icon = icon , title = title , summary = summary , onClick = {
            onClick()
        })
    }
}

/**
 * Displays a category header within preference screens.
 *
 * This composable function renders a distinct header for preference categories, enhancing the visual organization of settings screens. It uses a primary color and semi-bold text styling to clearly distinguish the category title from individual preference items.
 *
 * @param title The text to be displayed as the category header. This should clearly and concisely name the preference category.
 */
@Composable
fun PreferenceCategoryItem(
    title : String
) {
    Text(
        text = title , color = MaterialTheme.colorScheme.primary , style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold) , modifier = Modifier.padding(start = SizeConstants.LargeSize , top = SizeConstants.LargeSize)
    )
}

/**
 * Creates a clickable preference item for app preference screens.
 *
 * This composable function displays a preference item with an optional icon, title, and summary. The entire row is clickable and triggers the provided `onClick` callback function when clicked.
 *
 * @param icon An optional icon to be displayed at the start of the preference item. If provided, it should be an `ImageVector` object.
 * @param title An optional main title text displayed for the preference item.
 * @param summary An optional secondary text displayed below the title for additional information about the preference.
 * @param onClick A callback function that is called when the entire preference item is clicked. If no action is needed on click, this can be left empty.
 */
@Composable
fun PreferenceItem(
    icon : ImageVector? = null , title : String? = null , summary : String? = null , enabled : Boolean = true , rippleEffectDp : Dp = SizeConstants.LargeSize , onClick : () -> Unit = {}
) {
    val view : View = LocalView.current
    Row(
        modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = rippleEffectDp))
                .clickable(enabled = enabled , onClick = {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    onClick()
                }) , verticalAlignment = Alignment.CenterVertically
    ) {
        icon?.let {
            LargeHorizontalSpacer()
            Icon(imageVector = it , contentDescription = null)
        }
        Column(
            modifier = Modifier.padding(all = SizeConstants.LargeSize)
        ) {
            title?.let {
                Text(
                    text = it , style = MaterialTheme.typography.titleLarge , color = if (! enabled) LocalContentColor.current.copy(alpha = 0.38f) else LocalContentColor.current
                )
            }
            summary?.let {
                Text(
                    text = it , style = MaterialTheme.typography.bodyMedium , color = if (! enabled) LocalContentColor.current.copy(alpha = 0.38f) else LocalContentColor.current
                )
            }
        }
    }
}

/**
 * Creates a clickable preference item with a switch for app preference screens.
 *
 * This composable function combines an optional icon, title, optional summary, and a switch into a single row.
 * The entire row is clickable and toggles the switch when clicked, calling the provided `onCheckedChange` callback function with the new state.
 *
 * @param icon An optional icon to be displayed at the start of the preference item. If provided, it should be an `ImageVector` object.
 * @param title The main title text displayed for the preference item.
 * @param summary An optional secondary text displayed below the title for additional information about the preference.
 * @param checked The initial state of the switch. Set to true for on and false for off.
 * @param onCheckedChange A callback function that is called whenever the switch is toggled. This function receives the new state of the switch (boolean) as a parameter.
 */
@Composable
fun SwitchPreferenceItem(
    icon : ImageVector? = null , title : String , summary : String? = null , checked : Boolean , onCheckedChange : (Boolean) -> Unit
) {
    val view : View = LocalView.current
    Row(
        modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = SizeConstants.LargeSize))
                .clickable(onClick = {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    onCheckedChange(! checked)
                }) , verticalAlignment = Alignment.CenterVertically
    ) {
        icon?.let {
            LargeHorizontalSpacer()
            Icon(imageVector = it , contentDescription = null)
            LargeHorizontalSpacer()
        }
        Column(
            modifier = Modifier
                    .padding(all = SizeConstants.LargeSize)
                    .weight(weight = 1f)
        ) {
            Text(text = title , style = MaterialTheme.typography.titleLarge)
            summary?.let {
                Text(text = it , style = MaterialTheme.typography.bodyMedium)
            }
        }
        Switch(checked = checked , onCheckedChange = { isChecked ->
            onCheckedChange(isChecked)
        } , modifier = Modifier.padding(all = SizeConstants.LargeSize))
    }
}

/**
 * Creates a clickable preference item with a switch and a divider for app preference screens.
 *
 * This composable function combines an optional icon, title, summary, switch, and a divider into a single row.
 * The entire row is clickable and triggers the provided `onClick` callback function when clicked.
 * The switch is toggled on or off based on the `checked` parameter, and any change in its state calls
 * the `onCheckedChange` callback with the new state.
 *
 * @param icon An optional icon to be displayed at the start of the preference item. If provided, it should be an `ImageVector` object.
 * @param title The main title text displayed for the preference item.
 * @param summary A secondary text displayed below the title for additional information about the preference.
 * @param checked The initial state of the switch. Set to true for on and false for off.
 * @param onCheckedChange A callback function that is called whenever the switch is toggled. This function receives the new state of the switch (boolean) as a parameter.
 * @param onClick A callback function that is called when the entire preference item is clicked. If no action is needed on click, this can be left empty.
 */
@Composable
fun SwitchPreferenceItemWithDivider(
    icon : ImageVector? = null , title : String , summary : String , checked : Boolean , onCheckedChange : (Boolean) -> Unit , onClick : () -> Unit , onSwitchClick : (Boolean) -> Unit
) {
    val view : View = LocalView.current
    Row(
        modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = SizeConstants.LargeSize))
                .clickable(onClick = {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    onClick()
                }) , verticalAlignment = Alignment.CenterVertically
    ) {
        icon?.let {
            LargeHorizontalSpacer()
            Icon(imageVector = it , contentDescription = null)
            LargeHorizontalSpacer()
        }
        Column(
            modifier = Modifier
                    .padding(all = SizeConstants.LargeSize)
                    .weight(weight = 1f)
        ) {
            Text(text = title , style = MaterialTheme.typography.titleLarge)
            Text(text = summary , style = MaterialTheme.typography.bodyMedium)
        }

        VerticalDivider(
            modifier = Modifier
                    .height(height = 32.dp)
                    .align(alignment = Alignment.CenterVertically) , color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f) , thickness = 1.dp
        )
        Switch(checked = checked , onCheckedChange = { isChecked ->
            onCheckedChange(isChecked)
            onSwitchClick(isChecked)
        } , modifier = Modifier.padding(all = SizeConstants.LargeSize))
    }
}

/**
 * A composable function that creates a radio button preference item.
 *
 * This item displays a text label and a radio button. Clicking on the item toggles the radio button's state.
 *
 * @param text The text to display next to the radio button.
 * @param isChecked Whether the radio button is currently checked.
 * @param onCheckedChange A callback that is invoked when the radio button's state changes.
 *                        It provides the new checked state as a Boolean parameter.
 */
@Composable
fun RadioButtonPreferenceItem(
    text : String ,
    isChecked : Boolean ,
    onCheckedChange : (Boolean) -> Unit ,
) {
    Row(modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = SizeConstants.LargeSize))
            .clickable { onCheckedChange(! isChecked) } , verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = text , style = MaterialTheme.typography.titleLarge , modifier = Modifier
                    .weight(weight = 1f)
                    .padding(end = SizeConstants.LargeSize , start = SizeConstants.LargeSize)
        )
        RadioButton(selected = isChecked , onClick = { onCheckedChange(! isChecked) })
    }
}

/**
 * A composable function that creates a preference item with a checkbox.
 *
 * This item displays an optional icon, a title, an optional summary, and a checkbox.
 * Clicking the item toggles the checkbox state and triggers the provided [onCheckedChange] callback.
 *
 * @param icon The optional icon to display at the start of the item.
 * @param title The main title text for the preference item.
 * @param summary The optional summary text to display below the title.
 * @param checked The current checked state of the checkbox.
 * @param onCheckedChange A callback function that is invoked when the checkbox state changes.
 *                       It receives the new checked state as a boolean parameter.
 */
@Composable
fun CheckBoxPreferenceItem(
    icon : ImageVector? = null , title : String , summary : String? = null , checked : Boolean , onCheckedChange : (Boolean) -> Unit
) {
    Row(modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = SizeConstants.LargeSize))
            .clickable {
                onCheckedChange(! checked)
            } , verticalAlignment = Alignment.CenterVertically) {
        icon?.let {
            LargeHorizontalSpacer()
            Icon(it , contentDescription = null)
            LargeHorizontalSpacer()
        }
        Column(
            modifier = Modifier
                    .padding(all = SizeConstants.LargeSize)
                    .weight(weight = 1f)
        ) {
            Text(text = title , style = MaterialTheme.typography.titleLarge)
            summary?.let {
                Text(text = it , style = MaterialTheme.typography.bodyMedium)
            }
        }
        Checkbox(checked = checked , onCheckedChange = { isChecked ->
            onCheckedChange(isChecked)
        } , modifier = Modifier.padding(start = SizeConstants.LargeSize))
    }
}