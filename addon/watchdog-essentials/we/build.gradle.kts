import com.hypherionmc.modpublisher.properties.CurseEnvironment
import com.hypherionmc.modpublisher.properties.ModLoader
import plugins.modDisplayName
import plugins.modVersion
import tasks.GenerateIndexHtmlTask

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.benManesVersions)
    alias(libs.plugins.kotest)
    alias(libs.plugins.modPublisher)
    idea
    id("moddev.base")
    id("moddev.forge")
    id("moddev.mod")
    `maven-publish`
}

modDev {
    dependsOn(
        projects.addon.watchdogEssentials.common.path,
        projects.addon.watchdogEssentials.feature.lowTps.path,
        projects.addon.watchdogEssentials.feature.lowMemory.path,
        projects.addon.watchdogEssentials.feature.emptyServer.path,
    )

    toml {
        dependency("oms") {
            mandatory.set(true)
            versionRange.set("[1.0.0,)")
        }
    }

    setupMod {
        id.set(WatchdogEssentials.ID)
        version.set(WatchdogEssentials.VERSION)
        displayName.set(WatchdogEssentials.DISPLAY_NAME)
        description.set(WatchdogEssentials.DESCRIPTION)
        groupId.set(WatchdogEssentials.GROUP_ID)
        license.set(WatchdogEssentials.LICENSE)
        authors.set(WatchdogEssentials.AUTHOR)
    }
}

dependencies {
    modLocalRuntime(projects.oms)
    modCompileOnly(libs.omsApi)
    implementation(projects.omsCommon)

    implementation(libs.kotlinforforge)
}

publisher {
    apiKeys {
        // Modrinth Token
        modrinth(System.getenv("MODRINTH_FORGE_API_KEY"))
        // Curseforge Token
        curseforge(System.getenv("CURSE_FORGE_API_KEY"))
    }

    curseID.set("1666565")
    modrinthID.set("optheKB5")
    versionType.set("release")
    changelog.set(file("CHANGELOG.md"))
    version.set(project.version.toString())
    projectVersion.set(libs.versions.minecraft.get() + "-" + project.version.toString())
    setJavaVersions(JavaVersion.VERSION_17)
    displayName.set("$modDisplayName $modVersion")
    setGameVersions(libs.versions.minecraft.get())
    setLoaders(ModLoader.FORGE, ModLoader.NEOFORGE)
    setCurseEnvironment(CurseEnvironment.SERVER)
    artifact.set("build/libs/${project.base.archivesName.get()}-${project.version}.jar")

    curseDepends {
        required("kotlin-for-forge")
        required("operate-my-server")
    }
    modrinthDepends {
        required("kotlin-for-forge")
        required("operate-my-server")
    }
}

java {
    withSourcesJar()
}

tasks.matching { it.name == "sourcesJar" }.configureEach {
    dependsOn("mergeModLangFiles")
}

publishing {
    publications {
        create<MavenPublication>("we") {
            from(components["java"])
            groupId = WatchdogEssentials.GROUP_ID
            artifactId = WatchdogEssentials.ID
            version = WatchdogEssentials.VERSION

            pom {
                name.set(WatchdogEssentials.DISPLAY_NAME)
                description.set(WatchdogEssentials.DESCRIPTION)
                url.set("https://github.com/c0nnor263/OperateMyServer")
                licenses {
                    license {
                        name.set(WatchdogEssentials.LICENSE)
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set(WatchdogEssentials.AUTHOR)
                        name.set(WatchdogEssentials.AUTHOR)
                    }
                }
                scm {
                    url.set("https://github.com/c0nnor263/OperateMyServer")
                }
            }
        }
    }

    repositories {
        maven {
            name = "OperateMyServerPages"
            url = uri("${rootProject.projectDir}/gh-pages/maven")
        }
    }
}

val checkRemoteVersionNotPublished =
    tasks.register<tasks.CheckRemoteVersionNotPublishedTask>("checkRemoteVersionNotPublished") {
        groupId.set(WatchdogEssentials.GROUP_ID)
        artifactId.set(WatchdogEssentials.ID)
        version.set(WatchdogEssentials.VERSION)
        baseUrl.set("https://c0nnor263.github.io/OperateMyServer/maven")
    }


val generateIndexHtmlTask = tasks.register<GenerateIndexHtmlTask>("generateIndexHtml") {
    this.title.set("Index of /gh-pages")
    outputDir.set(rootProject.projectDir.resolve("gh-pages"))
}

tasks.register("publishWatchdogEssentials") {
    group = "publishing"
    description =
        "Runs tests, checks remote version, publishes WatchdogEssentials to local Maven (GitHub Pages) and updates index.html"

    dependsOn(
        "test",
        "checkRemoteVersionNotPublished",
        "publishWePublicationToOperateMyServerPagesRepository"
    )

    finalizedBy(generateIndexHtmlTask)
}

tasks.named("publishMod"){
    dependsOn(
        "test",
        "publishCurseforge",
        "publishModrinth"
    )
}