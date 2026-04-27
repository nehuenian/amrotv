# Build File Templates

## Convention Plugins

All modules use convention plugins from `build-logic/` instead of raw AGP/KGP boilerplate.
Each module picks only the plugins it needs — they compose cleanly.

| Plugin ID | Purpose |
|-----------|---------|
| `amro.kotlin.library` | Pure Kotlin JVM module — applies `kotlin.jvm`, sets `jvmToolchain(11)` |
| `amro.android.library` | Base Android library — applies `com.android.library`, sets `compileSdk=36`, `minSdk=24`, `compileOptions`, jvmTarget=11 |
| `amro.android.hilt` | Hilt DI — applies `hilt` + `ksp` plugins |
| `amro.android.compose` | Jetpack Compose — applies `kotlin.compose`, enables `buildFeatures.compose` |

> Convention plugin IDs are declared as TOML aliases (no version needed for included-build plugins):
> ```toml
> [plugins]
> amro-kotlin-library = { id = "amro.kotlin.library" }
> amro-android-library = { id = "amro.android.library" }
> amro-android-hilt    = { id = "amro.android.hilt" }
> amro-android-compose = { id = "amro.android.compose" }
> ```

### Inter-module dependencies

Always use **type-safe project accessors** (`projects.*`). Enable once in `settings.gradle.kts`:
```kotlin
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
```
Then reference modules as:
```kotlin
implementation(projects.core.mvi)
implementation(projects.libraries.logger.api)
```

---

## Pure-Kotlin module (`:libraries:logger:api`, `:feature:*:domain:api`)

Modules with no Android APIs use `amro.kotlin.library`:

```kotlin
plugins {
    alias(libs.plugins.amro.kotlin.library)
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
}
```

---

## Android Library module (general template)

```kotlin
plugins {
    alias(libs.plugins.amro.android.library)
}

android {
    namespace = "nl.abnamro.amrotv.<module.path>"
}
```

> `compileSdk`, `minSdk`, `compileOptions`, and `jvmTarget` are all set by the convention plugin.

---

## `:core:mvi` build.gradle.kts

`:core:mvi` is an **Android library** (not pure Kotlin) because it depends on `androidx.lifecycle:viewmodel-ktx`.

```kotlin
plugins {
    alias(libs.plugins.amro.android.library)
}

android {
    namespace = "nl.abnamro.amrotv.core.mvi"
}

dependencies {
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.kotlinx.coroutines.core)
}
```

---

## `:feature:*:domain:implementation` build.gradle.kts

```kotlin
plugins {
    alias(libs.plugins.amro.android.library)
    alias(libs.plugins.amro.android.hilt)
}

android {
    namespace = "{featurePackage}.domain.implementation"
}

dependencies {
    implementation(projects.feature.movies.domain.api)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
```

---

## `:feature:*:data` build.gradle.kts

```kotlin
plugins {
    alias(libs.plugins.amro.android.library)
    alias(libs.plugins.amro.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "{featurePackage}.data"
}

dependencies {
    implementation(projects.feature.movies.domain.api)
    implementation(projects.core.network)
    implementation(projects.libraries.logger.api)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.json)
}
```

---

## `:feature:*:presentation:api` build.gradle.kts

```kotlin
plugins {
    alias(libs.plugins.amro.android.library)
}

android {
    namespace = "{featurePackage}.presentation.api"
}

dependencies {
    implementation(projects.feature.movies.domain.api)
    implementation(projects.core.mvi)
}
```

---

## `:feature:*:presentation:implementation` build.gradle.kts

```kotlin
plugins {
    alias(libs.plugins.amro.android.library)
    alias(libs.plugins.amro.android.hilt)
}

android {
    namespace = "{featurePackage}.presentation.implementation"
}

dependencies {
    implementation(projects.feature.movies.presentation.api)
    implementation(projects.feature.movies.domain.api)
    implementation(projects.core.mvi)
    implementation(projects.libraries.logger.api)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.kotlinx.coroutines.android)
}
```

---

## `:feature:*:ui` build.gradle.kts

```kotlin
plugins {
    alias(libs.plugins.amro.android.library)
    alias(libs.plugins.amro.android.compose)
    alias(libs.plugins.amro.android.hilt)
}

android {
    namespace = "{featurePackage}.ui"
}

dependencies {
    implementation(projects.feature.movies.presentation.api)
    implementation(projects.core.ui)
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
