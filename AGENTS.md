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

## UI/UX guidelines
@./docs/ui-ux-guidelines.md

## Coding style

@./docs/style-guidance.md

## Jetpack Compose architecture
@./docs/jetpack-compose-layering.md

## Jetpack Compose best practices
@./docs/compose-best-practices.md

## State and Jetpack Compose
@./docs/compose-state.md

## Thinking in Compose
@./docs/thinking-in-compose.md

## Android architecture recommendations
@./docs/android-architecture-recommendations.md

## AGENTS usage
@./docs/gemini-agents.md

## Coroutines best practices
@./docs/coroutines-best-practices.md

## Kotlin coroutines on Android
@./docs/kotlin-coroutines-android.md

## Kotlin flows on Android
@./docs/kotlin-flows-android.md

## Testing Kotlin flows on Android
@./docs/testing-kotlin-flows-android.md

## Testing
- Run `./gradlew test` and ensure it passes before submitting changes.
