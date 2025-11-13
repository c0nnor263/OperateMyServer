import tasks.GenerateIndexHtmlTask

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.modDevGradle)
    alias(libs.plugins.benManesVersions)
    alias(libs.plugins.kotest)
    idea
    `maven-publish`
}

apply<ModDevPlugin>()

legacyForge {
    version = libs.versions.forge.get()

    parchment {
        mappingsVersion = libs.versions.parchment.get()
        minecraftVersion = libs.versions.minecraft.get()
    }

    addModdingDependenciesTo(sourceSets.test.get())
}

dependencies {
    testImplementation(projects.omsTesting)
}

java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("omsApi") {
            from(components["java"])
            groupId = OmsApi.GROUP_ID
            artifactId = OmsApi.ID
            version = OmsApi.VERSION

            pom {
                name.set(OmsApi.DISPLAY_NAME)
                description.set(OmsApi.DESCRIPTION)
                url.set("https://github.com/c0nnor263/OperateMyServer")
                licenses {
                    license {
                        name.set(OmsApi.LICENSE)
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set(OmsApi.AUTHOR)
                        name.set(OmsApi.AUTHOR)
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
            name = "OperateMyServerDocs"
            url = uri("${rootProject.projectDir}/docs/oms-api")
        }
    }
}

val checkRemoteVersionNotPublished =
    tasks.register<tasks.CheckRemoteVersionNotPublishedTask>("checkRemoteVersionNotPublished") {
        groupId.set(OmsApi.GROUP_ID)
        artifactId.set(OmsApi.ID)
        version.set(OmsApi.VERSION)
        baseUrl.set("https://c0nnor263.github.io/OperateMyServer/oms-api")
    }


val generateIndexHtmlTask = tasks.register<GenerateIndexHtmlTask>("generateIndexHtml") {
    title.set("Index of /docs")
    outputDir.set(rootProject.projectDir.resolve("docs"))
}

tasks.register("publishOmsApi") {
    group = "publishing"
    description =
        "Runs tests, checks remote version, publishes oms-api to local Maven (GitHub Pages) and updates index.html"

    dependsOn(
        "jvmKotest",
        "checkRemoteVersionNotPublished",
        "publishOmsApiPublicationToOperateMyServerDocsRepository"
    )

    finalizedBy(generateIndexHtmlTask)
}