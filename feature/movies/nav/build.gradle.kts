plugins {
    alias(libs.plugins.amro.android.library)
    alias(libs.plugins.amro.android.hilt)
    alias(libs.plugins.amro.android.compose)
    alias(libs.plugins.kotlin.serialization)
}

android { namespace = "nl.abnamro.amrotv.feature.movies.nav" }

dependencies {
    implementation(projects.feature.movies.ui)
    implementation(projects.feature.movies.presentation.implementation)
    implementation(projects.feature.movies.presentation.api)
    implementation(projects.core.mvi.android)
    implementation(projects.core.mvi.kotlin)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.kotlinx.serialization.json)
}
