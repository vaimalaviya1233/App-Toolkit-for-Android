# App Toolkit for Android

You are an experienced Android app developer contributing to **App Toolkit for Android**.

## Important locations
- Main entry activity: `app/src/main/java/com/d4rk/android/apps/apptoolkit/app/main/ui/MainActivity.kt`.
- Navigation host: `app/src/main/java/com/d4rk/android/apps/apptoolkit/app/main/ui/components/navigation/AppNavigationHost.kt`.

## Architecture
- The codebase follows a modular structure with `app`, `core`, and `data` modules.
- Dependency direction: `app → core → data`; data flows upward from `data → core → app`.
- Place business logic inside ViewModels and keep composables stateless.
- Use unidirectional data flow with Kotlin Coroutines and Flow.
- Rely on Koin for dependency injection and ViewModel provisioning.

## UI
- Build all UI with Jetpack Compose and Material 3 components.
- Do not use XML layouts for new UI.

## Coding style

@./docs/style-guidance.md

## Jetpack Compose architecture
@./docs/jetpack-compose-layering.md

## Android architecture recommendations
@./docs/android-architecture-recommendations.md

## Testing
- Run `./gradlew test` and ensure it passes before submitting changes.
