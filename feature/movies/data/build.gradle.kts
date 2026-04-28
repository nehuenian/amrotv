plugins {
    alias(libs.plugins.amro.android.library)
    alias(libs.plugins.amro.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "nl.abnamro.amrotv.feature.movies.data"
}

dependencies {
    implementation(projects.feature.movies.domain.api)
    implementation(projects.core.data)
    implementation(projects.libraries.logger.api)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
