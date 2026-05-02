import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension

class AmroDetektConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("io.gitlab.arturbosch.detekt")
            val libs = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")
            extensions.configure(DetektExtension::class.java) {
                config.setFrom(rootProject.file("config/detekt/detekt.yml"))
                buildUponDefaultConfig = true
                parallel = true
            }
            dependencies.add("detektPlugins", libs.findLibrary("detekt-compose-rules").get())
        }
    }
}
