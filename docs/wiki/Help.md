# Help

## Layers
- **Data**: Supplies help links and support resources.
- **Domain**: Processes help requests and navigation events.
- **UI**: `HelpScreen` composable presented by `HelpActivity`.

## Primary Screens
- `HelpScreen` – displays FAQ entries and support contact options.

## Integration
```kotlin
startActivity(Intent(context, HelpActivity::class.java))
```
