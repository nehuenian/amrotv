enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "AMROTV"
include(":app")
include(":core:mvi:kotlin")
include(":core:mvi:android")
include(":core:network")
include(":core:ui")
include(":libraries:logger:api")
include(":libraries:logger:implementation")
include(":feature:movies:domain:api")
include(":feature:movies:domain:implementation")
include(":feature:movies:data")
include(":feature:movies:presentation:api")
include(":feature:movies:presentation:implementation")
include(":feature:movies:ui")
 