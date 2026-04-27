plugins {
    alias(libs.plugins.amro.android.library)
    alias(libs.plugins.amro.android.hilt)
}

android {
    namespace = "nl.abnamro.amrotv.core.network"
}

dependencies {
    implementation(projects.libraries.logger.api)
    implementation(projects.core.buildConfig)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
