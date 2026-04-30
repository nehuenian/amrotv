---
name: amro-ui-conventions
description: >
  AMRO project-specific UI conventions for Jetpack Compose screens and components.
  Captures all alignment decisions made during the movies UI layer implementation:
  MVI intent routing, composable purity, state hoisting rules, LazyGrid sticky header
  pattern, design tokens, preview conventions, and architecture layer constraints.
  Auto-load this skill whenever reviewing, writing, or refactoring any file in
  feature/movies/ui/ or any *Screen.kt, *Content.kt file. If in doubt, load it first.
  These rules take precedence over generic compose-skill guidance where they conflict.
---

# AMRO UI Conventions

Project-specific rules for the `feature/movies/ui` module. These were established
iteratively through code review and design alignment — every rule here has a reason.

---

## 1. Composable Purity: Screen vs Content

The **Screen** composable is stateful and is the **only** composable that:
- Owns the `viewModel` reference
- Collects `effects` via `LaunchedEffect(Unit)`
- Calls real navigation callbacks (`onNavigateBack`, `onOpenMovieDetail`)
- Wires `onIntent = viewModel::handleIntent`

The **Content** composable (and everything below it) has **exactly one action parameter**:

```kotlin
fun TrendingMoviesContent(
    state: TrendingMoviesState,
    onIntent: (TrendingMoviesIntent) -> Unit,
    modifier: Modifier = Modifier,
)
```

No `onRetry`, no `onNavigateBack`, no `onToggleSortOrder`, no `onBack`. **Everything is an intent.**

> Why: a single `onIntent` channel makes composables portable, previewable with `onIntent = {}`,
> and ensures all user interactions are tracked through MVI.

---

## 2. Navigation Goes Through Effects, Not Callbacks

Navigation callbacks (`onNavigateBack`, `onOpenDetail`) belong **only** in the Screen — never in Content or child composables.

**Pattern:**
1. User taps back → `onIntent(MovieDetailIntent.NavigateBack)`
2. ViewModel handles it → `sendEffect(MovieDetailEffect.NavigateBack)`
3. Screen `LaunchedEffect` collects it → calls `onNavigateBack()`

```kotlin
// ✅ correct — back button in Content
IconButton(onClick = { onIntent(MovieDetailIntent.NavigateBack) }) { ... }

// ❌ wrong — back button in Content
IconButton(onClick = onNavigateBack) { ... }
```

---

## 3. Retry Goes Through onIntent, Not a Separate Callback

No **Content or intermediate composable** ever has `onRetry: () -> Unit` as a parameter. Retry is an intent dispatched at the Screen boundary. Leaf display components (like `InlineErrorBanner`) may expose `onRetry: () -> Unit`, but the caller must always pass `{ onIntent(XxxIntent.Retry) }`.

```kotlin
// ✅ correct — leaf component exposes onRetry, caller dispatches through intent
InlineErrorBanner(errors = errors, onRetry = { onIntent(MovieDetailIntent.Retry) })

// ❌ wrong — Content composable propagates a raw lambda instead of dispatching intent
fun MovieDetailBody(onRetry: () -> Unit, ...)
```

**ViewModel stores context for retry** — `currentMovieId` private field so `Retry` needs no payload:

```kotlin
private var currentMovieId: Int = -1

is MovieDetailIntent.LoadMovieDetail -> {
    currentMovieId = intent.movieId
    loadDetail(intent.movieId)
}
is MovieDetailIntent.Retry -> loadDetail(currentMovieId)
```

---

## 4. No Logic in onClick

Never put guard conditions inside `onClick` lambdas — let the ViewModel decide:

```kotlin
// ✅ correct
onClick = { onIntent(TrendingMoviesIntent.SelectSortOrder(order)) }

// ❌ wrong
onClick = { if (sortOrder != order) onIntent(TrendingMoviesIntent.SelectSortOrder(order)) }
```

---

## 5. UI Visibility State Lives in MVI State

If a UI element's visibility can be triggered by user interaction (and should be trackable/testable), it belongs in `State` — not in local `remember` inside a composable.

```kotlin
// ✅ correct — in TrendingMoviesState
val showSortSheet: Boolean = false

// Intent
data class SetSortSheetVisible(val visible: Boolean) : TrendingMoviesIntent

// Content reads from state; never owns it
val showSortSheet = state.showSortSheet
```

Use a **single intent with a boolean payload** — not separate `Open`/`Close` intents:

```kotlin
// ✅ one intent
data class SetSortSheetVisible(val visible: Boolean) : TrendingMoviesIntent

// ❌ two intents
data object OpenSortSheet : TrendingMoviesIntent
data object CloseSortSheet : TrendingMoviesIntent
```

---

## 6. Animation Details Stay in the Composable

Animation state (`sheetState`, `coroutineScope`) that exists purely to sequence visual transitions is **not** business logic and stays inside the composable:

```kotlin
// ✅ SortControl owns animation — this is fine
val sheetState = rememberModalBottomSheetState()
val coroutineScope = rememberCoroutineScope()

// dismiss: play animation first, then notify parent
coroutineScope.launch {
    sheetState.hide()        // suspend — plays slide-down
    onShowSheetChange(false) // then update state
}
```

Without `sheetState.hide()` the sheet snaps away instantly. This coroutine is animation sequencing, not business logic.

---

## 7. No remember for Action Lambdas

Wrapping callbacks in `remember` for "stability" is cargo-cult optimization in MVI. Content recomposes on every state change anyway, and wrapping lambdas in `remember` with an unstable key (inline lambda) always misses the cache:

```kotlin
// ✅ correct — just wire directly
onIntent = viewModel::handleIntent

// ❌ wrong — useless in MVI
val stableOnIntent = remember(viewModel) { viewModel::handleIntent }
```

---

## 8. derivedStateOf — Only When Output Is Coarser Than Input

`derivedStateOf` is only beneficial when the derived value changes far less often than the source state. If the output changes on every source update, it adds overhead with no benefit.

```kotlin
// ✅ Boolean from scroll position — coarser, derivedStateOf is correct
val isGenreFilterVisible by remember(gridState) {
    derivedStateOf { gridState.layoutInfo.visibleItemsInfo.any { it.key == "genre_filter" } }
}

// ❌ Float alpha from scroll offset — changes every frame, just use graphicsLayer
val bannerAlpha by remember(gridState) {
    derivedStateOf { gridState.firstVisibleItemScrollOffset / maxOffset.toFloat() }
}
// Instead: read State<Float> inside graphicsLayer { alpha = bannerAlphaState.value }
```

---

## 11. Banner Full-Bleed in LazyVerticalGrid

When a grid has horizontal `contentPadding`, items are constrained to the inner width. For a full-bleed banner that scrolls with the grid:

```kotlin
val bleedModifier = with(LocalDensity.current) {
    val bleedPx = contentPadding.roundToPx()
    Modifier.layout { measurable, constraints ->
        val expanded = constraints.maxWidth + bleedPx * 2
        val placeable = measurable.measure(
            constraints.copy(minWidth = expanded, maxWidth = expanded)
        )
        layout(constraints.maxWidth, placeable.height) { placeable.place(-bleedPx, 0) }
    }
}
```

---

## 12. LaunchedEffect — One Block With Unit Key

When a screen's side-effect key never changes during the screen's lifetime (e.g., `movieId` never changes once the screen is alive), use `LaunchedEffect(Unit)` — not `LaunchedEffect(movieId)`. Merge all effects into one block:

```kotlin
// ✅ correct
LaunchedEffect(Unit) {
    viewModel.handleIntent(MovieDetailIntent.LoadMovieDetail(movieId))
    viewModel.effects.collect { effect ->
        when (effect) {
            is MovieDetailEffect.NavigateBack -> onNavigateBack()
            is MovieDetailEffect.OpenUrl -> currentUriHandler.openUri(effect.url)
        }
    }
}

// ❌ wrong — two separate LaunchedEffect blocks; movieId never changes
LaunchedEffect(movieId) { viewModel.handleIntent(...) }
LaunchedEffect(Unit) { viewModel.effects.collect { ... } }
```

---

## 14. Computed Values Belong in StateReducers, Not Composables

Any value derived from non-UI data (dates, formatted strings, business rules) must be computed in `StateReducers.initialState()` or a reducer — never in `remember {}` in a composable.

```kotlin
// ✅ correct — in TrendingMoviesStateReducers
fun initialState(): TrendingMoviesState = TrendingMoviesState(
    weekRangeLabel = computeWeekRangeLabel()
)

// ❌ wrong — in composable
val weekRangeLabel = remember { computeWeekRangeLabel() }
```

> Why: `remember {}` in a composable violates "same state → same UI" — the value
> depends on when the composable was first composed, not on the state.

---

## 15. Design Tokens — Colors and Shapes

**Never hardcode colors.** Use `AmroTvColors.*` tokens:

```kotlin
// ✅
containerColor = AmroTvColors.MediaScrimTransparent

// ❌
containerColor = Color.Transparent
```

**Chip/control shapes use `CircleShape`** — not `MaterialTheme.shapes.small`:

```kotlin
// ✅ FilterChip, ElevatedAssistChip, SortChip
shape = CircleShape

// ❌ default — too square
// (no explicit shape = uses shapes.small = 8dp)
```

**Buttons on dark backdrops use on-media tokens:**

```kotlin
// ✅ OutlinedButton on a movie backdrop
ButtonDefaults.outlinedButtonColors(contentColor = AmroTvColors.OnMediaPrimary)
border = BorderStroke(1.dp, AmroTvColors.OnMediaPrimary)

// ❌ default primary (green) is invisible/wrong on dark image
```

---

## 16. Preview Conventions

**One `@Preview` function per composable** using `@PreviewParameter`:

```kotlin
// ✅
@Preview
@Composable
private fun TrendingMoviesContentPreview(
    @PreviewParameter(TrendingMoviesStateProvider::class) state: TrendingMoviesState,
) {
    AmroTvTheme { TrendingMoviesContent(state = state, onIntent = {}) }
}

// ❌ multiple separate @Preview functions with inline data
@Preview @Composable private fun PreviewLoading() { ... }
@Preview @Composable private fun PreviewError() { ... }
```

**State providers are `internal` classes** in a dedicated `preview/` sub-package:

```
ui/
└── trendingmovies/
    └── preview/
        ├── TrendingMoviesStateProvider.kt   ← internal class
        └── MoviePreviewData.kt              ← internal object, shared data
```

**Each provider must cover these states:**

| State | Required |
|-------|---------|
| Loading (no data) | ✅ |
| Error (no data) | ✅ |
| Success (full data) | ✅ |
| Partial error (data + inline error) | ✅ |
| Multiple errors | ✅ |

**Never use `showSystemUi = true` for screens with a `TopAppBar`** — it creates a double status bar artifact.

---

## 18. Multiple Errors in State

`state.errors` is always `ImmutableList<MovieError>` — never a single nullable error. When rendering:

- **Full-screen error** (no data): show `errors.first()` — one error is enough to block the screen
- **Inline error banner** (partial data present): pass the full `errors` list to `InlineErrorBanner` so all active errors are visible

```kotlin
// Full-screen: first error only
AmroTvErrorView(message = stringResource(state.errors.first().toStringResId()), ...)

// Inline: all errors
InlineErrorBanner(errors = state.errors, onRetry = { onIntent(SomeIntent.Retry) })
```
