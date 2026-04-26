package plugins

import net.neoforged.moddevgradle.legacyforge.dsl.LegacyForgeExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.SourceSetContainer
import plugins.extension.ModDevExtension

class ModDevForgePlugin : Plugin<Project> {
    override fun apply(project: Project) = with(project) {
        val libs = extensions
            .getByType(VersionCatalogsExtension::class.java)
            .named("libs")

        pluginManager.apply("net.neoforged.moddev.legacyforge")

        extensions.configure<LegacyForgeExtension>("legacyForge") {
            version = libs.findVersion("minecraft").get().requiredVersion + "-" + libs.findVersion("forge")
                .get().requiredVersion

            parchment {
                mappingsVersion.set(
                    libs.findVersion("parchment").get().requiredVersion
                )
                minecraftVersion.set(
                    libs.findVersion("minecraft").get().requiredVersion
                )
            }
        }

        afterEvaluate {
            val modDev = extensions.getByType(ModDevExtension::class.java)
            if (modDev.includeTest.get()) {
                val sourceSets =
                    extensions.getByType(SourceSetContainer::class.java)

                extensions.configure<LegacyForgeExtension>("legacyForge") {
                    addModdingDependenciesTo(sourceSets.getByName("test"))
                }
            }
        }
    }
}