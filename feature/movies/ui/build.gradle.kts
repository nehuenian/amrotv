plugins {
  alias(libs.plugins.amro.android.library)
  alias(libs.plugins.amro.android.compose)
}

android { namespace = "nl.abnamro.amrotv.feature.movies.ui" }

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
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.activity.compose)
  implementation(libs.coil.compose)
}
