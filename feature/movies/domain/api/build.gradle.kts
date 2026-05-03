plugins { alias(libs.plugins.amro.kotlin.library) }

dependencies {
    api(projects.core.domain)
    implementation(libs.kotlinx.coroutines.core)
}
