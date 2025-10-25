import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.repositories
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class ModDevPlugin : Plugin<Project> {
    override fun apply(project: Project) = with(project) {

        tasks.withType<JavaCompile>().configureEach {
            options.encoding = "UTF-8"
            options.release.set(17)
        }

        tasks.withType<KotlinCompile>().configureEach {
            compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
        }

        repositories {
            mavenCentral()
            maven {
                url = uri("https://thedarkcolour.github.io/KotlinForForge/")
                content { includeGroup("thedarkcolour") }
            }
            maven("https://maven.parchmentmc.org") // Parchment mappings
            maven("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/") // Forge Config API Port
            maven("https://c0nnor263.github.io/OperateMyServer/oms-api/")
        }

        extensions.configure<KotlinJvmProjectExtension>("kotlin") {
            jvmToolchain(17)
        }

        // IDEA
        extensions.configure<IdeaModel>("idea") {
            module {
                isDownloadSources = true
                isDownloadJavadoc = true
            }
        }
    }
}