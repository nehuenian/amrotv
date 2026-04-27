---
name: create-screen
description: >
  Add a new MVI screen to an existing AMRO feature module.
  Creates State, Intent, Effect, ViewModel, Screen composable, and Content composable.
  Use when the user wants to add a new screen to an existing feature, says things like
  "add a screen for [X]", "create a [Name]Screen", "I need a new page/view for [Y]",
  or asks to wire up a new ViewModel + Compose screen in any existing feature module.
  Also use when the user wants to implement a specific UI flow inside an existing feature.
argument-hint: <ScreenName> in <feature-name>
user-invocable: true
---

# Create Screen: $ARGUMENTS

Add a new screen to an existing AMRO feature.

Read `.github/skills/architecture-reference/SKILL.md` before proceeding.

## Steps

> Before writing any code, read the nearest existing screen in `feature/movies/ui/` as a structural reference (once Commit 10 exists). The templates below show the required skeleton — the actual file is the source of truth for imports and exact patterns.

### 1. Parse Arguments
Argument format: `<ScreenName> in <feature-name>` (e.g. `MovieSearch in movies`)
- **ScreenName** = PascalCase screen name (e.g. `MovieSearch`)
- **featureName** = kebab-case feature (e.g. `movies`)
- **packageSegment** = lowercase no hyphens (e.g. `movies`)
- **featurePackage** = `nl.abnamro.amrotv.feature.{packageSegment}`

### 2. Create presentation/api files

**{ScreenName}State.kt**
```kotlin
package {featurePackage}.presentation.api

import nl.abnamro.amrotv.core.mvi.MviState

data class {ScreenName}State(
    val isLoading: Boolean = false,
    val error: String? = null,
    // TODO: screen-specific fields
) : MviState
```

**{ScreenName}Intent.kt**
```kotlin
package {featurePackage}.presentation.api

import nl.abnamro.amrotv.core.mvi.MviIntent

sealed interface {ScreenName}Intent : MviIntent {
    data object Load : {ScreenName}Intent
    data object Retry : {ScreenName}Intent
    // TODO: screen-specific intents
}
```

**{ScreenName}Effect.kt**
```kotlin
package {featurePackage}.presentation.api

import nl.abnamro.amrotv.core.mvi.MviEffect

sealed interface {ScreenName}Effect : MviEffect {
    data class ShowError(val message: String) : {ScreenName}Effect
    // TODO: screen-specific effects (navigation, etc.)
}
```

### 3. Create presentation/implementation file

**{ScreenName}ViewModel.kt**
```kotlin
package {featurePackage}.presentation.implementation

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import nl.abnamro.amrotv.core.mvi.MviViewModel
import nl.abnamro.amrotv.libraries.api.Logger
import {featurePackage}.presentation.api.{ScreenName}Effect
import {featurePackage}.presentation.api.{ScreenName}Intent
import {featurePackage}.presentation.api.{ScreenName}State
import javax.inject.Inject

@HiltViewModel
class {ScreenName}ViewModel @Inject constructor(
    // TODO: inject use cases
    private val logger: Logger,
) : MviViewModel<{ScreenName}State, {ScreenName}Intent, {ScreenName}Effect>(
    initialState = {ScreenName}State()
) {
    init { handleIntent({ScreenName}Intent.Load) }

    override fun handleIntent(intent: {ScreenName}Intent) {
        when (intent) {
            {ScreenName}Intent.Load, {ScreenName}Intent.Retry -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            updateState { copy(isLoading = true, error = null) }
            // TODO: call use case, collect, update state
        }
    }
}
```

### 4. Create ui file

**{ScreenName}Screen.kt**
```kotlin
package {featurePackage}.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.abnamro.amrotv.core.ui.AmroTheme
import nl.abnamro.amrotv.core.ui.ErrorView
import nl.abnamro.amrotv.core.ui.LoadingView
import {featurePackage}.presentation.api.{ScreenName}Effect
import {featurePackage}.presentation.api.{ScreenName}Intent
import {featurePackage}.presentation.api.{ScreenName}State
import {featurePackage}.presentation.implementation.{ScreenName}ViewModel

@Composable
fun {ScreenName}Screen(
    // TODO: add navigation callbacks
    viewModel: {ScreenName}ViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is {ScreenName}Effect.ShowError -> { /* TODO: show snackbar */ }
            }
        }
    }

    {ScreenName}Content(
        state = state,
        onIntent = viewModel::handleIntent,
    )
}

@Composable
fun {ScreenName}Content(
    state: {ScreenName}State,
    onIntent: ({ScreenName}Intent) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        state.isLoading -> LoadingView(modifier = modifier)
        state.error != null -> ErrorView(
            message = state.error,
            onRetry = { onIntent({ScreenName}Intent.Retry) },
            modifier = modifier,
        )
        else -> {
            // TODO: render screen content
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun {ScreenName}ContentPreview() {
    AmroTheme {
        {ScreenName}Content(
            state = {ScreenName}State(),
            onIntent = {},
        )
    }
}
```

### 5. Add route in :app navigation (Navigation3)

In `AmroNavHost.kt`, add a `@Serializable` route type and an `entryProvider` entry:

```kotlin
// Route — plain @Serializable type in :app (data object if no params, data class if params needed)
@Serializable data class {ScreenName}Route(/* params if needed */)

// Inside entryProvider { ... } block in NavDisplay:
addEntryProvider<{ScreenName}Route> { key ->
    NavEntry(key) {
        {ScreenName}Screen(
            // pass route params from key, e.g.: someParam = key.someParam,
            onNavigateBack = { backStack.removeLastOrNull() },
        )
    }
}
```

To navigate to this screen from another screen, emit an effect from the ViewModel and handle it in the caller Screen:

```kotlin
// In the calling Screen's LaunchedEffect:
is SomeEffect.NavigateTo{ScreenName} -> backStack.add({ScreenName}Route(/* params */))
```

Rules:
- Routes live only in `:app` — screens never import `NavDisplay` or manipulate `backStack` directly.
- Use `data object` for routes with no parameters, `data class` for routes with parameters.
- `hiltViewModel()` scopes the ViewModel per back-stack entry via `rememberViewModelStoreNavEntryDecorator()`.
```
