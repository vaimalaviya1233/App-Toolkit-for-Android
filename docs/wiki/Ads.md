# Ads

## Layers
- **Data**: Implements data sources for advertising preferences.
- **Domain**: Contains use cases and business rules around ad configuration.
- **UI**: `AdsSettingsScreen` composable wrapped by `AdsSettingsActivity`.

## Primary Screens
- `AdsSettingsScreen` – allows users to manage ad related options.

## Integration
```kotlin
startActivity(Intent(context, AdsSettingsActivity::class.java))
```
