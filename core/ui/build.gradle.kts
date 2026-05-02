plugins {
    alias(libs.plugins.amro.android.library)
    alias(libs.plugins.amro.android.compose)
}

android { namespace = "nl.abnamro.amrotv.core.ui" }

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
