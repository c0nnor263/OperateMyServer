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

val omsModules = listOf(
    projects.omsApi,
    projects.omsCommon,
    projects.feature.scheduledRestart
)

modDev {
    includeTest.set(true)

    dependsOn(
        *omsModules
            .map { it.path }
            .toTypedArray()
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
        // Modrinth Token
        modrinth(System.getenv("MODRINTH_FORGE_API_KEY"))
        // Curseforge Token
        curseforge(System.getenv("CURSE_FORGE_API_KEY"))
    }

    curseID.set("1341025")
    modrinthID.set("ZZEpAgHx")
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
    }
    modrinthDepends {
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
    this.title.set("Index of /gh-pages")
    outputDir.set(rootProject.projectDir.resolve("gh-pages"))
}

val testOperateMyServer = tasks.register("testOperateMyServer") {
    group = "verification"
    description = "Runs tests for all Operate My Server modules"

    dependsOn(
        omsModules.map { "${it.path}:test" }
    )

    dependsOn("test")
}

tasks.register("publishOperateMyServer") {
    group = "publishing"
    description =
        "Runs tests, checks remote version, publishes OperateMyServer to local Maven (GitHub Pages) and updates index.html"

    dependsOn(
        testOperateMyServer,
        "checkRemoteVersionNotPublished",
        "publishOmsPublicationToOperateMyServerPagesRepository"
    )

    finalizedBy(generateIndexHtmlTask)
}

tasks.named("publishMod") {
    dependsOn(
        testOperateMyServer,
        "publishCurseforge",
        "publishModrinth"
    )
}