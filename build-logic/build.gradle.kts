plugins { `kotlin-dsl` }

group = "nl.abnamro.amrotv.buildlogic"

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.android.gradleApi)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.detekt.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("amroKotlinLibrary") {
            id = "amro.kotlin.library"
            implementationClass = "AmroKotlinLibraryConventionPlugin"
        }
        register("amroAndroidLibrary") {
            id = "amro.android.library"
            implementationClass = "AmroAndroidLibraryConventionPlugin"
        }
        register("amroAndroidHilt") {
            id = "amro.android.hilt"
            implementationClass = "AmroAndroidHiltConventionPlugin"
        }
        register("amroAndroidCompose") {
            id = "amro.android.compose"
            implementationClass = "AmroAndroidComposeConventionPlugin"
        }
        register("amroDetekt") {
            id = "amro.detekt"
            implementationClass = "AmroDetektConventionPlugin"
        }
    }
}
