plugins {
    alias(libs.plugins.amro.android.library)
    alias(libs.plugins.amro.android.hilt)
}

android { namespace = "nl.abnamro.amrotv.feature.movies.presentation.implementation" }

dependencies {
    implementation(projects.feature.movies.presentation.api)
    implementation(projects.feature.movies.domain.api)
    implementation(projects.core.mvi.android)
    implementation(projects.core.mvi.kotlin)
    implementation(projects.libraries.logger.api)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}

tasks.withType<Test> { useJUnitPlatform() }
