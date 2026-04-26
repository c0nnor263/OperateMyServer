package plugins

import net.neoforged.moddevgradle.legacyforge.dsl.LegacyForgeExtension
import net.neoforged.moddevgradle.legacyforge.dsl.ObfuscationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.tasks.Jar
import org.gradle.language.jvm.tasks.ProcessResources
import org.slf4j.event.Level
import plugins.extension.ModDevExtension
import tasks.GenerateModsTomlTask
import tasks.MergeLangFilesTask
import tasks.ModTomlDependencyInput

class ModDevModPlugin : Plugin<Project> {
    override fun apply(project: Project) = with(project) {
        val libs = extensions
            .getByType(VersionCatalogsExtension::class.java)
            .named("libs")
        val basePlugin = extensions.getByType(BasePluginExtension::class.java)
        val modDev = extensions.getByType(ModDevExtension::class.java)
        val sourceSets = extensions.getByType(SourceSetContainer::class.java)

        configureLocalRuntime()

        project.pluginManager.withPlugin("moddev.forge") {
            modDev.onSetupModConfigured {
                require(modDev.setupMod.id.isPresent) {
                    "moddev.mod applied, but modDev.setupMod.id is not set"
                }
                group = modGroupId
                version = "${modVersion}+mc${libs.findVersion("minecraft").get().requiredVersion}"
                basePlugin.archivesName.set(modId)
                sourceSets
                    .getByName("main")
                    .resources
                    .srcDir(layout.buildDirectory.dir("generated/resources"))

                runForEachDependentProject(modDev) { dependentProject ->
                    evaluationDependsOn(dependentProject.path)
                    dependencies.add("implementation", dependentProject)
                }

                tasks.named("jar", Jar::class.java).configure {
                    runForEachDependentProject(modDev) { dependentProject ->
                        val depSourceSets =
                            dependentProject.extensions.getByType(SourceSetContainer::class.java)

                        from(depSourceSets.getByName("main").output)
                    }
                }
                configureOmsLegacyForge(modDev)
                configureMergeLangFilesTask(modDev)
                configureGenerateModsTomlTask(modDev)
            }
        }
    }

    fun Project.configureLocalRuntime(){
        val localRuntime = configurations.maybeCreate("localRuntime")
        configurations.named("runtimeClasspath") {
            extendsFrom(localRuntime)
        }

        extensions.configure<ObfuscationExtension>("obfuscation") {
            createRemappingConfiguration(localRuntime)
        }
    }

    fun Project.configureOmsLegacyForge(
        modDev: ModDevExtension
    ) {
        val sourceSets = extensions.getByType(SourceSetContainer::class.java)
        val mainSourceSet = sourceSets.getByName("main")
        extensions.configure<LegacyForgeExtension>("legacyForge") {
            mods {
                create(modId) {
                    sourceSet(mainSourceSet)
                    runForEachDependentProject(modDev) { dependentProject ->
                        val depSourceSets =
                            dependentProject.extensions.getByType(SourceSetContainer::class.java)

                        sourceSet(depSourceSets.getByName("main"))
                    }
                }
            }

            runs {
                configureEach {
                    systemProperty("forge.logging.markers", "REGISTRIES")
                    logLevel.set(Level.DEBUG)

                    jvmArgument("-XX:+IgnoreUnrecognizedVMOptions")
                    jvmArgument("-XX:+UnlockExperimentalVMOptions")
                }

                create("client") {
                    client()
                    systemProperty(
                        "forge.enabledGameTestNamespaces",
                        modId
                    )
                }

                create("server") {
                    server()
                    systemProperty(
                        "forge.enabledGameTestNamespaces",
                        modId
                    )
                }
            }
        }
    }

    private fun Project.configureMergeLangFilesTask(
        modDev: ModDevExtension,
    ) {
        val mergedLangDir =
            layout.buildDirectory.dir("generated/mergedLang/assets/$modId/lang")

        val mergeLangFiles = tasks.register("mergeModLangFiles", MergeLangFilesTask::class.java) {
            this.modId.set(this@configureMergeLangFilesTask.modId)
            outputDir.set(mergedLangDir)
            projectPaths.set(modDev.dependentProjects)
        }

        tasks.withType(ProcessResources::class.java).configureEach {
            dependsOn(mergeLangFiles)
            exclude("assets/$modId/lang/*.json")
            from(mergedLangDir) {
                into("assets/$modId/lang")
            }
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        }

        runForEachDependentProject(modDev) { dependentProject ->
            dependentProject.tasks.withType(ProcessResources::class.java).configureEach {
                exclude("assets/$modId/lang/*.json")
            }
        }
    }

    private fun Project.configureGenerateModsTomlTask(
        modDev: ModDevExtension
    ) {
        val libs = extensions
            .getByType(VersionCatalogsExtension::class.java)
            .named("libs")

        val generateModsToml = tasks.register(
            "generateModsToml",
            GenerateModsTomlTask::class.java
        ) {
            modId.set(project.modId)
            modVersion.set(project.modVersion)
            displayName.set(project.modDisplayName)
            authors.set(project.modAuthors)
            modDescription.set(project.modDescription)
            license.set(project.modLicense)

            minecraftVersion.set(
                libs.findVersion("minecraft").get().requiredVersion
            )

            forgeVersion.set(
                libs.findVersion("forge").get().requiredVersion
            )

            loaderVersion.set(
                libs.findVersion("kotlinForForge").get().requiredVersion
            )

            outputDir.set(
                project.layout.buildDirectory.dir("generated/resources/META-INF")
            )

            extras.set(modDev.toml.extras)

            dependencies.set(
                modDev.toml.dependencies.map { dep ->
                    ModTomlDependencyInput(
                        modId = dep.modId.get(),
                        mandatory = dep.mandatory.get(),
                        versionRange = dep.versionRange.get(),
                        ordering = dep.ordering.get(),
                        side = dep.side.get()
                    )
                }
            )
        }

        extensions.configure<LegacyForgeExtension>("legacyForge") {
            ideSyncTask(generateModsToml)
        }
    }

    private fun Project.runForEachDependentProject(
        modDev: ModDevExtension,
        action: (Project) -> Unit
    ) {
        modDev.dependentProjects.get().forEach { path ->
            val project = findProject(path)
                ?: error("Dependent project '$path' not found")
            action(project)
        }
    }
}
