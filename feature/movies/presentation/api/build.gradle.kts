plugins { alias(libs.plugins.amro.android.library) }

android { namespace = "nl.abnamro.amrotv.feature.movies.presentation.api" }

dependencies {
    // api() — not implementation() — because :ui consumes SortOption/SortOrder from domain:api
    // and AmroTvViewModel from core:mvi:kotlin directly in Screen/component signatures.
    // These types must be visible to any module that depends on :presentation:api.
    api(projects.feature.movies.domain.api)
    api(projects.core.mvi.kotlin)
    implementation(libs.kotlinx.collections.immutable)
}
