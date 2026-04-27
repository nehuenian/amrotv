plugins {
    alias(libs.plugins.amro.android.library)
    alias(libs.plugins.amro.android.hilt)
}

android {
    namespace = "nl.abnamro.amrotv.libraries.logger.implementation"
}

dependencies {
    implementation(projects.libraries.logger.api)
    implementation(libs.timber)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
