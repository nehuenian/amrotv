plugins {
    alias(libs.plugins.amro.android.library)
    alias(libs.plugins.amro.android.hilt)
}

android {
    namespace = "nl.abnamro.amrotv.feature.movies.presentation.implementation"
}

dependencies {
    implementation(projects.feature.movies.presentation.api)
    implementation(projects.feature.movies.domain.api)
    implementation(projects.core.mvi)
    implementation(projects.libraries.logger.api)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
