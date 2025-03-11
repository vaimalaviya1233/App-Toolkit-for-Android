# AppToolkit

AppToolkit is a versatile Android library designed to streamline development by providing pre-built components and utilities that adhere to modern design principles.

## Features

- **Pre-built Ktor Client**: A ready-to-use Ktor client for efficient network operations.
- **Material Design Spacers**: Predefined spacers that align with Material Design guidelines.
- **Android 15 Style Preferences**: Customizable settings preferences styled after Android 15.
- **Clipboard Helper**: Utilities for seamless clipboard interactions.
- **Intent Helper**: Simplified handling of Android intents.
- **AboutLibraries Integration**: Easy redirection to [AboutLibraries by Mike Penz](https://github.com/mikepenz/AboutLibraries) for displaying open-source licenses.
- **ScreenHelper**: A utility class to determine screen types (e.g., tablet, phone, orientation).

## Installation

To integrate AppToolkit into your project, add the following dependency to your `build.gradle` file:

### **Kotlin DSL**:

```kotlin
dependencies {
    api("com.github.D4rK7355608:AppToolkit:+") {
        isTransitive = true
    }
}
```

### **Groovy DSL**:

```groovy
dependencies {
    api('com.github.D4rK7355608:AppToolkit:+') {
        transitive = true
    }
}
```

**Note**: Enabling transitive dependencies ensures that all necessary dependencies included in AppToolkit are available in your project, simplifying setup and reducing potential conflicts.

## Usage

### **Ktor Client**

Initialize and use the pre-built Ktor client for network requests:

```kotlin
val client = KtorClient().createClient()
// Use 'client' to make network requests
```

### **Material Design Spacers**

Implement spacers in your Compose UI to maintain consistent spacing:

```kotlin
import com.d4rk.android.libs.apptoolkit.ui.components.spacers.*

Row {
    Text("Item 1")
    LargeHorizontalSpacer()
    Text("Item 2")
}
```

### **Android 15 Style Preferences**

Integrate Android 15 styled preferences into your settings screen:

```kotlin
import com.d4rk.android.libs.apptoolkit.ui.components.preferences.SwitchPreferenceItem

SwitchPreferenceItem(
    title = "Enable Feature",
    checked = true,
    onCheckedChange = { isChecked ->
        // Handle switch state change
    }
)
```

### **Clipboard Helper**

Copy text to the clipboard with ease:

```kotlin
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.ClipboardHelper

ClipboardHelper.copyTextToClipboard(context, "Label", "Sample text")
```

### **Intent Helper**

Launch activities or share content using Intent Helper:

```kotlin
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.IntentsHelper

IntentsHelper.shareApp(context, R.string.share_message)
```

### **AboutLibraries Integration**

Redirect users to the AboutLibraries screen to display open-source licenses:

```kotlin
IntentsHelper.openLicensesScreen(
    context = context,
    eulaHtmlString = "Your EULA content",
    changelogHtmlString = "Your changelog content",
    appName = "My App",
    appVersion = "1.0.0",
    appVersionCode = 1,
    appShortDescription = R.string.app_description
)
```

## Contributing

Contributions are welcome! Please fork the repository and submit a pull request. For major changes, open an issue first to discuss your ideas.

## License

This project is licensed under the GNU General Public License v3.0. See the [LICENSE](LICENSE) file for details.

## Contact

For any questions or suggestions, feel free to reach out to me at [d4rk7355608@gmail.com](mailto:d4rk7355608@gmail.com).