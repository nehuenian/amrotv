---
name: create-feature-module
description: >
  Scaffold all 6 sub-modules for a new AMRO feature
  (data, domain:api, domain:implementation, presentation:api, presentation:implementation, ui)
  with build files, source directories, and template code.
  Use this skill whenever the user wants to create a new feature module set, add a new feature
  to the app, scaffold boilerplate for a new domain area, or says anything like "add a feature",
  "create a [name] module", "I need a new feature for [X]", or "set up the modules for [Y]".
  Even if the user just names a feature without explicitly asking to scaffold, use this skill.
argument-hint: <feature-name>
user-invocable: true
---

# Create Feature Module: $ARGUMENTS

Scaffold a complete new feature named **$ARGUMENTS** for AMRO.

Read the architecture reference at `.github/skills/architecture-reference/SKILL.md` before proceeding.

## Steps

### 1. Derive Names

From argument `$ARGUMENTS` (kebab-case, e.g. `actor-info`):
- **featureName** = kebab-case as-is (e.g. `actor-info`)
- **FeatureName** = PascalCase (e.g. `ActorInfo`)
- **packageSegment** = lowercase no hyphens (e.g. `actorinfo`)
- **rootPackage** = `nl.abnamro.amrotv`
- **featurePackage** = `nl.abnamro.amrotv.feature.{packageSegment}`
- **moduleBase** = `:feature:{featureName}` (e.g. `:feature:actor-info`)

### 2. Create Directory Structure

```
feature/{featureName}/
├── data/
│   └── src/main/kotlin/{featurePackage}/data/
├── domain/
│   ├── api/
│   │   └── src/main/kotlin/{featurePackage}/domain/api/
│   └── implementation/
│       └── src/main/kotlin/{featurePackage}/domain/implementation/
└── presentation/
    ├── api/
    │   └── src/main/kotlin/{featurePackage}/presentation/api/
    ├── implementation/
    │   └── src/main/kotlin/{featurePackage}/presentation/implementation/
    └── ui/
        └── src/main/kotlin/{featurePackage}/ui/
```

### 3. Register in settings.gradle.kts

Add to `settings.gradle.kts`:
```kotlin
include(":feature:{featureName}:domain:api")
include(":feature:{featureName}:domain:implementation")
include(":feature:{featureName}:data")
include(":feature:{featureName}:presentation:api")
include(":feature:{featureName}:presentation:implementation")
include(":feature:{featureName}:ui")
```

### 4. Create build.gradle.kts Files

**domain/api/build.gradle.kts** — pure Kotlin, no Android:
```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
}
```

**domain/implementation/build.gradle.kts**:
```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "{featurePackage}.domain.implementation"
    compileSdk = 36
    defaultConfig { minSdk = 24 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions { jvmTarget = "11" }
}

dependencies {
    implementation(project(":feature:{featureName}:domain:api"))
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
```

**data/build.gradle.kts**:
```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "{featurePackage}.data"
    compileSdk = 36
    defaultConfig { minSdk = 24 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions { jvmTarget = "11" }
}

dependencies {
    implementation(project(":feature:{featureName}:domain:api"))
    implementation(project(":core:network"))
    implementation(project(":libraries:api"))
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.json)
}
```

**presentation/api/build.gradle.kts**:
```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "{featurePackage}.presentation.api"
    compileSdk = 36
    defaultConfig { minSdk = 24 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions { jvmTarget = "11" }
}

dependencies {
    implementation(project(":feature:{featureName}:domain:api"))
    implementation(project(":core:mvi"))
}
```

**presentation/implementation/build.gradle.kts**:
```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "{featurePackage}.presentation.implementation"
    compileSdk = 36
    defaultConfig { minSdk = 24 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions { jvmTarget = "11" }
}

dependencies {
    implementation(project(":feature:{featureName}:presentation:api"))
    implementation(project(":feature:{featureName}:domain:api"))
    implementation(project(":core:mvi"))
    implementation(project(":libraries:api"))
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.kotlinx.coroutines.android)
}
```

**ui/build.gradle.kts**:
```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "{featurePackage}.ui"
    compileSdk = 36
    defaultConfig { minSdk = 24 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions { jvmTarget = "11" }
    buildFeatures { compose = true }
}

dependencies {
    implementation(project(":feature:{featureName}:presentation:api"))
    implementation(project(":core:ui"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.coil.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
```

### 5. Create Template Code

#### domain/api — Domain model + Repository + Use case interfaces

```kotlin
// {featurePackage}.domain.api.{FeatureName}.kt
data class {FeatureName}(
    val id: Int,
    // TODO: add fields
)

interface {FeatureName}Repository {
    fun get{FeatureName}(): Flow<List<{FeatureName}>>
}

fun interface Get{FeatureName}UseCase {
    operator fun invoke(): Flow<List<{FeatureName}>>
}
```

#### domain/implementation — Use case impl + Hilt module

```kotlin
class Get{FeatureName}UseCaseImpl @Inject constructor(
    private val repository: {FeatureName}Repository,
) : Get{FeatureName}UseCase {
    override fun invoke(): Flow<List<{FeatureName}>> = repository.get{FeatureName}()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class {FeatureName}DomainModule {
    @Binds abstract fun bindGet{FeatureName}(impl: Get{FeatureName}UseCaseImpl): Get{FeatureName}UseCase
}
```

#### data — DTO + Mapper + Repository impl + Hilt module

```kotlin
@Serializable
data class {FeatureName}Dto(
    @SerialName("id") val id: Int,
    // TODO: add fields
)

fun {FeatureName}Dto.toDomain(): {FeatureName} = {FeatureName}(id = id)

class {FeatureName}RepositoryImpl @Inject constructor(
    private val apiService: {FeatureName}ApiService,
) : {FeatureName}Repository {
    override fun get{FeatureName}(): Flow<List<{FeatureName}>> = flow {
        emit(apiService.get{FeatureName}().map { it.toDomain() })
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class {FeatureName}DataBindingsModule {
    @Binds abstract fun bind{FeatureName}Repository(impl: {FeatureName}RepositoryImpl): {FeatureName}Repository
}
```

#### presentation/api — MVI contracts

```kotlin
// State
data class {FeatureName}State(
    val isLoading: Boolean = false,
    val items: List<{FeatureName}> = emptyList(),
    val error: String? = null,
) : MviState

// Intent
sealed interface {FeatureName}Intent : MviIntent {
    data object Load : {FeatureName}Intent
    data object Retry : {FeatureName}Intent
}

// Effect
sealed interface {FeatureName}Effect : MviEffect {
    data class ShowError(val message: String) : {FeatureName}Effect
}
```

#### presentation/implementation — ViewModel

```kotlin
@HiltViewModel
class {FeatureName}ViewModel @Inject constructor(
    private val get{FeatureName}: Get{FeatureName}UseCase,
    private val logger: Logger,
) : MviViewModel<{FeatureName}State, {FeatureName}Intent, {FeatureName}Effect>(
    initialState = {FeatureName}State()
) {
    init { handleIntent({FeatureName}Intent.Load) }

    override fun handleIntent(intent: {FeatureName}Intent) {
        when (intent) {
            {FeatureName}Intent.Load, {FeatureName}Intent.Retry -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            updateState { copy(isLoading = true, error = null) }
            get{FeatureName}()
                .catch { e ->
                    logger.e("{FeatureName}ViewModel", "Load failed", e)
                    updateState { copy(isLoading = false, error = e.message) }
                }
                .collect { items ->
                    updateState { copy(isLoading = false, items = items) }
                }
        }
    }
}
```

#### ui — Screen + Content composable

```kotlin
@Composable
fun {FeatureName}Screen(
    onNavigateBack: () -> Unit,
    viewModel: {FeatureName}ViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is {FeatureName}Effect.ShowError -> { /* show snackbar */ }
            }
        }
    }
    {FeatureName}Content(state = state, onIntent = viewModel::handleIntent)
}

@Composable
fun {FeatureName}Content(
    state: {FeatureName}State,
    onIntent: ({FeatureName}Intent) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        state.isLoading -> LoadingView()
        state.error != null -> ErrorView(message = state.error, onRetry = { onIntent({FeatureName}Intent.Retry) })
        else -> {
            // TODO: render state.items
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun {FeatureName}ContentPreview() {
    AmroTheme {
        {FeatureName}Content(state = {FeatureName}State(), onIntent = {})
    }
}
```
