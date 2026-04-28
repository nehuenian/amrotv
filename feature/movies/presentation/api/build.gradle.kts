plugins {
    alias(libs.plugins.amro.android.library)
}

android {
    namespace = "nl.abnamro.amrotv.feature.movies.presentation.api"
}

dependencies {
    implementation(projects.feature.movies.domain.api)
    implementation(projects.core.mvi.kotlin)
    implementation(libs.kotlinx.collections.immutable)
}
