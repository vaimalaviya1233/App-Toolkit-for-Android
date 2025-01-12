# AppToolkit

AppToolkit is a versatile Android library designed to streamline development by providing pre-built
components and utilities that adhere to modern design principles.

## Features

- **Pre-built Ktor Client**: A ready-to-use Ktor client for efficient network operations.
- **Material Design Spacers**: Predefined spacers that align with Material Design guidelines.
- **Android 15 Style Preferences**: Customizable settings preferences styled after Android 15.
- **Clipboard Helper**: Utilities for seamless clipboard interactions.
- **Intent Helper**: Simplified handling of Android intents.
- **AboutLibraries Integration**: Easy redirection
  to [AboutLibraries by Mike Penz](https://github.com/mikepenz/AboutLibraries) for displaying
  open-source licenses.

## Installation

To integrate AppToolkit into your project, add the following dependency to your `build.gradle` file:

For **Kotlin DSL**:

```kotlin
dependencies {
    api("com.github.D4rK7355608:AppToolkit:0.0.7") {
        isTransitive = true
    }
}
```

For **Groovy DSL**:

```groovy
dependencies {
    api('com.github.D4rK7355608:AppToolkit:0.0.7') {
        transitive = true
    }
}
```

*Note*: Enabling transitive dependencies ensures that all necessary dependencies included in
AppToolkit are available in your project, simplifying setup and reducing potential conflicts.

## Usage

Here's how to utilize some of the components provided by AppToolkit:

### Ktor Client

Initialize and use the pre-built Ktor client for network requests:

```kotlin
import com.example.apptoolkit.network.KtorClient

val client = KtorClient.instance
// Use 'client' to make network requests
```

### Material Design Spacers

Implement spacers in your Compose UI to maintain consistent spacing:

```kotlin
import com.example.apptoolkit.ui.Spacers

Spacer(modifier = Modifier.height(Spacers.medium))
```

### Android 15 Style Preferences

Integrate Android 15 styled preferences into your settings screen:

```kotlin
import com.example.apptoolkit.preferences.PreferenceScreen

PreferenceScreen {
    // Define your preferences here
}
```

### Clipboard Helper

Copy text to the clipboard with ease:

```kotlin
import com.example.apptoolkit.utils.ClipboardHelper

ClipboardHelper.copyToClipboard(context , "Sample text")
```

### Intent Helper

Launch activities or share content using Intent Helper:

```kotlin
import com.example.apptoolkit.utils.IntentHelper

IntentHelper.shareText(context , "Check out AppToolkit!")
```

### AboutLibraries Integration

Redirect users to the AboutLibraries screen to display open-source licenses:

```kotlin
IntentsHelper.openLicensesScreen(
    context = context ,
    eulaHtmlString = eulaHtmlString ,
    changelogHtmlString = changelogHtmlString ,
    appName = R.string.app_name ,
    appVersion = BuildConfig.VERSION_NAME ,
    appVersionCode = BuildConfig.VERSION_CODE ,
    appShortDescription = R.string.app_short_description
)
```

## Contributing

Contributions are welcome! Please fork the repository and submit a pull request. For major changes,
open an issue first to discuss your ideas.

## License

This project is licensed under the GNU General Public License v3.0. See the [LICENSE](LICENSE) file
for details.

## Contact

For any questions or suggestions, feel free to reach out to me
at [d4rk7355608@gmail.com](mailto:d4rk7355608@gmail.com).