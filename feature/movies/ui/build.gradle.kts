plugins {
    alias(libs.plugins.amro.android.library)
    alias(libs.plugins.amro.android.compose)
    alias(libs.plugins.amro.android.hilt)
    alias(libs.plugins.mannodermaus.android.junit5)
}

android {
    namespace = "nl.abnamro.amrotv.feature.movies.ui"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    implementation(projects.feature.movies.presentation.api)
    implementation(projects.core.ui)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.coil.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.hilt.android.testing)
    androidTestImplementation(libs.hilt.navigation.compose)
    androidTestImplementation(libs.androidx.lifecycle.viewmodel.compose)
    androidTestImplementation(libs.androidx.lifecycle.viewmodel.ktx)
    androidTestImplementation(projects.core.testing)
    androidTestImplementation(projects.core.mvi.android)
    androidTestImplementation(projects.core.buildConfig)
    androidTestImplementation(projects.core.data)
    androidTestImplementation(projects.feature.movies.domain.implementation)
    androidTestImplementation(projects.feature.movies.data)
    androidTestImplementation(projects.feature.movies.presentation.implementation)
    androidTestImplementation(projects.libraries.logger.implementation)
    androidTestImplementation(libs.okhttp.mockwebserver)
    androidTestImplementation(libs.kotlinx.serialization.json)
    androidTestImplementation(libs.retrofit)
    androidTestImplementation(libs.retrofit.kotlinx.serialization)
    kspAndroidTest(libs.hilt.compiler)
}
