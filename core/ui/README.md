# :core:ui — AMRO TV Theme & Shared Components

## Overview

The `:core:ui` module provides:

- **AmroTvTheme** — Material 3 theme using ABN Amro palette (green, gold, gray)
- **Shared Composables** — `AmroTvErrorView`, `AmroTvLoadingView`, `AmroTvEmptyView`

All components are prefixed with `AmroTv` to clearly identify them as part of the AMRO TV app.

## Color Palette

| Role | Color | Hex | Usage |
|------|-------|-----|-------|
| Primary | Green | #009488 | Main brand color, primary actions, highlights |
| Secondary | Gold | #F9BD20 | Accents, toggles, secondary actions |
| Tertiary | Gray | #878787 | Neutral secondary actions, disabled states |

**Note:** In Compose Material 3, passing only `primary`, `secondary`, and `tertiary` to `lightColorScheme()` or `darkColorScheme()` does not generate a full tonal palette from those brand colors. Any unspecified roles (such as background, error, and outline) continue to use the library's default values unless they are explicitly provided.

## Theme Setup

### Using AmroTvTheme

Wrap your app with `AmroTvTheme` at the top level. The theme will automatically detect
and apply the system light/dark mode setting:

```kotlin
@Composable
fun App() {
    AmroTvTheme {
        // Your app content here
        MyScreen()
    }
}
```

To override the system setting, pass an explicit value:

```kotlin
AmroTvTheme(darkTheme = true) {
    MyScreen()
}
```

The theme automatically applies:
- Material 3 color scheme with ABN Amro colors (follows system light/dark mode by default)
- Typography (display, headline, title, body, label styles)
- Shapes (extra-small to extra-large rounded corners)

### Accessing Theme Tokens

Use Material 3 token accessors within composables:

```kotlin
@Composable
fun MyComponent() {
    val primaryColor = MaterialTheme.colorScheme.primary
    val headlineStyle = MaterialTheme.typography.headlineMedium
    val mediumCorners = MaterialTheme.shapes.medium
    
    Text(
        text = "Hello AMRO TV",
        style = headlineStyle,
        color = primaryColor,
    )
}
```

## Shared Composables

### AmroTvErrorView

Displays an error state with icon, message, and optional retry button.

**Parameters:**
- `message: String` — Error message to display
- `modifier: Modifier` — Optional modifier
- `onRetry: (() -> Unit)?` — Optional retry callback

**Example:**

```kotlin
AmroTvErrorView(
    message = "Failed to load movies. Please check your connection.",
    onRetry = { viewModel.handleIntent(LoadMovies) },
)
```

### AmroTvLoadingView

Displays a loading state with centered progress indicator and optional message.

**Parameters:**
- `modifier: Modifier` — Optional modifier
- `message: String?` — Optional message to display

**Example:**

```kotlin
AmroTvLoadingView(message = "Loading movies...")
```

### AmroTvEmptyView

Displays an empty state with icon, title, and optional subtitle.

**Parameters:**
- `title: String` — Title text
- `modifier: Modifier` — Optional modifier
- `subtitle: String?` — Optional subtitle

**Example:**

```kotlin
AmroTvEmptyView(
    title = "No movies found",
    subtitle = "Try adjusting your filters or check back later.",
)
```

## Complete Screen Example

```kotlin
@Composable
fun MovieScreenExample(
    state: MovieState,
    onIntent: (MovieIntent) -> Unit,
) {
    when {
        state.isLoading -> AmroTvLoadingView()
        state.error != null -> AmroTvErrorView(
            message = state.error,
            onRetry = { onIntent(Retry) },
        )
        state.movies.isEmpty() -> AmroTvEmptyView(
            title = "No movies found",
            subtitle = "Be the first to discover trending movies.",
        )
        else -> MovieList(movies = state.movies)
    }
}
```

## Design Principles

- **Material 3 First** — All components follow Material 3 guidelines
- **Theme-Driven Styling** — No hardcoded colors; always use `MaterialTheme` tokens
- **Accessibility** — Sufficient contrast, readable text sizes, icon alternatives
- **Consistency** — All UI components share the same design language

## File Structure

```
core/ui/src/main/kotlin/nl/abnamro/amrotv/core/ui/
├── theme/
│   ├── AmroTvTheme.kt              — Main theme composable
│   ├── AmroTvColors.kt             — Color definitions
│   ├── AmroTvTypography.kt         — Typography styles
│   ├── AmroTvShapes.kt             — Shape definitions
│   └── AmroTvDimensions.kt         — Dimension values
└── component/
    ├── AmroTvErrorView.kt          — Error state component
    ├── AmroTvLoadingView.kt        — Loading state component
    └── AmroTvEmptyView.kt          — Empty state component
```

## Extending the Theme

To add new design tokens (e.g., custom spacing, additional colors):

1. **Add color constants** to `AmroTvColors.kt`
2. **Extend typography** in `AmroTvTypography.kt` if needed
3. **Update shape definitions** in `AmroTvShapes.kt`
4. **Add spacing/size values** to `AmroTvDimensions.kt` (centralized)
5. **Use tokens** via `MaterialTheme` accessors or `AmroTvDimensions`

Example: Adding a new custom dimension value

```kotlin
// Add to AmroTvDimensions object
object AmroTvDimensions {
    // ... existing values ...
    val customLarge = 32.dp
}

// Use it in composables
Box(modifier = Modifier.padding(AmroTvDimensions.customLarge))
```

## Testing

All components include `@Preview` composables showing light and dark mode variants:

- **AmroTvErrorView:** `AmroTvErrorViewLightPreview`, `AmroTvErrorViewDarkPreview`, `AmroTvErrorViewNoRetryLightPreview`, `AmroTvErrorViewNoRetryDarkPreview`
- **AmroTvLoadingView:** `AmroTvLoadingViewLightPreview`, `AmroTvLoadingViewDarkPreview`, `AmroTvLoadingViewWithMessageLightPreview`, `AmroTvLoadingViewWithMessageDarkPreview`
- **AmroTvEmptyView:** `AmroTvEmptyViewLightPreview`, `AmroTvEmptyViewDarkPreview`, `AmroTvEmptyViewWithSubtitleLightPreview`, `AmroTvEmptyViewWithSubtitleDarkPreview`

Run previews in Android Studio to verify visual consistency across light and dark modes.

## Launcher Icons

Custom launcher icons (TV screen with AMRO branding) are included in the `app/` module:

- **Pre-API 26 (devices without adaptive icon support):** Uses raster PNGs in:
  - `app/src/main/res/mipmap-mdpi/ic_launcher.png`
  - `app/src/main/res/mipmap-hdpi/ic_launcher.png`
  - `app/src/main/res/mipmap-xhdpi/ic_launcher.png`
  - `app/src/main/res/mipmap-xxhdpi/ic_launcher.png`
  - `app/src/main/res/mipmap-xxxhdpi/ic_launcher.png`

- **API 26+ (modern devices/TVs):** The manifest's `@mipmap/ic_launcher` resolves to the adaptive icon in `mipmap-anydpi-v26/ic_launcher.xml`, which references foreground/background drawable assets. These PNGs only affect pre-API 26 devices; to update the launcher icon on modern devices, update the adaptive icon definition in `mipmap-anydpi-v26/`.
