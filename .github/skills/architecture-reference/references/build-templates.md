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

### AGP 9.x rules

- **Never** add `kotlin.android` explicitly — AGP 9.x auto-applies it for all Android library modules. Adding it manually causes: `"Cannot add extension with name 'kotlin', as there is an extension already registered"`.
- Same for `kotlin.compose` — it also auto-applies `kotlin.android`.
- For jvmTarget, use `tasks.withType<KotlinJvmCompile>().configureEach { compilerOptions { jvmTarget.set(JvmTarget.JVM_11) } }` only when not using a convention plugin that already sets it.

### Inter-module dependencies

Always use type-safe project accessors (`projects.*`):

```kotlin
implementation(projects.core.mvi)
implementation(projects.libraries.logger.api)
```

---

## Canonical build.gradle.kts references

For the exact up-to-date template for each module type, read the actual file:

| Module type | Canonical reference |
|---|---|
| Pure Kotlin (`:libraries:logger:api`, `:feature:*:domain:api`, `:core:mvi:kotlin`) | `libraries/logger/api/build.gradle.kts` |
| Android library base (`:core:mvi:android`) | `core/mvi/android/build.gradle.kts` |
| Android library + Hilt (`:core:network`) | `core/network/build.gradle.kts` |
| Android library + Compose (`:core:ui`) | `core/ui/build.gradle.kts` |
| Android library + Hilt + Serialization (`:feature:movies:data`) | `feature/movies/data/build.gradle.kts` |
| Android library + Hilt (`:feature:movies:domain:implementation`) | `feature/movies/domain/implementation/build.gradle.kts` |
| Android library (`:feature:movies:presentation:api`) | `feature/movies/presentation/api/build.gradle.kts` |
| Android library + Hilt (`:feature:movies:presentation:implementation`) | `feature/movies/presentation/implementation/build.gradle.kts` |
| Android library + Compose + Hilt (`:feature:movies:ui`) | `feature/movies/ui/build.gradle.kts` |

> `:core:mvi` is split into `:core:mvi:kotlin` (pure Kotlin — interfaces, `AmroTvViewModel`) and `:core:mvi:android` (Android — `BaseAmroTvViewModel`, depends on `androidx.lifecycle`). Use `:core:mvi:kotlin` in `presentation:api`; use `:core:mvi:android` in `presentation:implementation`.
