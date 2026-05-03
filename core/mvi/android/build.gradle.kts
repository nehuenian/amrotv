plugins { alias(libs.plugins.amro.android.library) }

android { namespace = "nl.abnamro.amrotv.core.mvi" }

dependencies {
    implementation(projects.core.mvi.kotlin)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
}
