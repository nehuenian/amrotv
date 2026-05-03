import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class AmroAndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            pluginManager.withPlugin("com.android.library") {
                (extensions.getByName("android") as LibraryExtension).buildFeatures {
                    compose = true
                }
            }
            pluginManager.withPlugin("com.android.application") {
                (extensions.getByName("android") as ApplicationExtension).buildFeatures {
                    compose = true
                }
            }
        }
    }
}
