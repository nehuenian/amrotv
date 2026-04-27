plugins {
    alias(libs.plugins.amro.android.library)
    alias(libs.plugins.amro.android.hilt)
}

android {
    namespace = "nl.abnamro.amrotv.feature.movies.domain.implementation"
}

dependencies {
    implementation(projects.feature.movies.domain.api)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
