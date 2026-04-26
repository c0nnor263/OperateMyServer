
import com.hypherionmc.modpublisher.properties.CurseEnvironment
import com.hypherionmc.modpublisher.properties.ModLoader
import plugins.modDisplayName
import plugins.modVersion
import tasks.GenerateIndexHtmlTask

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.benManesVersions)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.kotest)
    alias(libs.plugins.modPublisher)
    idea
    id("moddev.base")
    id("moddev.forge")
    id("moddev.mod")
    `maven-publish`
}

modDev {
    includeTest.set(true)

    dependsOn(
        projects.omsApi.path,
        projects.omsCommon.path,
        projects.feature.scheduledRestart.path
    )

    setupMod {
        id.set(OperateMyServer.ID)
        version.set(OperateMyServer.VERSION)
        displayName.set(OperateMyServer.DISPLAY_NAME)
        description.set(OperateMyServer.DESCRIPTION)
        groupId.set(OperateMyServer.GROUP_ID)
        license.set(OperateMyServer.LICENSE)
        authors.set(OperateMyServer.AUTHOR)
    }
}

dependencies {
    implementation(libs.kotlinforforge)
    implementation(libs.kotlinxSerialization)

    testImplementation(projects.omsTesting)
}

publisher {
    apiKeys {
        curseforge(System.getenv("CURSE_FORGE_API_KEY"))
    }

    curseID.set("1341025")
    versionType.set("release")
    changelog.set(rootProject.file("CHANGELOG.md"))
    version.set(project.version.toString())
    displayName.set("$modDisplayName $modVersion")
    setGameVersions(libs.versions.minecraft.get())
    setLoaders(ModLoader.FORGE, ModLoader.NEOFORGE)
    setCurseEnvironment(CurseEnvironment.SERVER)
    artifact.set("build/libs/${project.base.archivesName.get()}-${project.version}.jar")

    curseDepends {
        required("kotlin-for-forge")
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
        create<MavenPublication>("oms") {
            from(components["java"])
            groupId = OperateMyServer.GROUP_ID
            artifactId = OperateMyServer.ID
            version = OperateMyServer.VERSION

            pom {
                name.set(OperateMyServer.DISPLAY_NAME)
                description.set(OperateMyServer.DESCRIPTION)
                url.set("https://github.com/c0nnor263/OperateMyServer")
                licenses {
                    license {
                        name.set(OperateMyServer.LICENSE)
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set(OperateMyServer.AUTHOR)
                        name.set(OperateMyServer.AUTHOR)
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
        groupId.set(OperateMyServer.GROUP_ID)
        artifactId.set(OperateMyServer.ID)
        version.set(OperateMyServer.VERSION)
        baseUrl.set("https://c0nnor263.github.io/OperateMyServer/maven")
    }


val generateIndexHtmlTask = tasks.register<GenerateIndexHtmlTask>("generateIndexHtml") {
    title.set("Index of /gh-pages")
    outputDir.set(rootProject.projectDir.resolve("gh-pages"))
}

tasks.register("publishOperateMyServer") {
    group = "publishing"
    description =
        "Runs tests, checks remote version, publishes OperateMyServer to local Maven (GitHub Pages) and updates index.html"

    dependsOn(
        "jvmKotest",
        "checkRemoteVersionNotPublished",
        "publishOmsPublicationToOperateMyServerPagesRepository"
    )

    finalizedBy(generateIndexHtmlTask)
}