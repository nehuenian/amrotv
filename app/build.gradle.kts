import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.amro.android.hilt)
    alias(libs.plugins.amro.detekt)
    alias(libs.plugins.kotlin.serialization)
}

private val tmdbReadAccessToken: String = run {
    val propsFile = rootProject.file("amrotv.properties")
    if (propsFile.exists()) {
        propsFile.inputStream().use { stream ->
            Properties().apply { load(stream) }.getProperty("TMDB_READ_ACCESS_TOKEN", "")
        }
    } else {
        ""
    }
}

android {
    namespace = "nl.abnamro.amrotv"
    compileSdk = 37

    defaultConfig {
        applicationId = "nl.abnamro.amrotv"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "TMDB_READ_ACCESS_TOKEN", "\"$tmdbReadAccessToken\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

// KGP 2.x + AGP 9.x: kotlin.android is applied automatically by kotlin.compose;
// kotlinOptions in android{} is not available at script-compile time so jvmTarget
// is set here explicitly instead.
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile>().configureEach {
    compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11) }
}

dependencies {
    implementation(projects.core.ui)
    implementation(projects.core.buildConfig)
    implementation(projects.feature.movies.nav)
    implementation(projects.feature.movies.data)
    implementation(projects.feature.movies.domain.implementation)
    implementation(projects.libraries.logger.implementation)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
