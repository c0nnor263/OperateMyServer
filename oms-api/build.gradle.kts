import tasks.GenerateIndexHtmlTask

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.benManesVersions)
    alias(libs.plugins.kotest)
    idea
    id("moddev.base")
    id("moddev.forge")
    `maven-publish`
}

modDev {
    includeTest.set(true)
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
            name = "OperateMyServerPages"
            url = uri("${rootProject.projectDir}/gh-pages/oms-api")
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
    title.set("Index of /gh-pages")
    outputDir.set(rootProject.projectDir.resolve("gh-pages"))
}

tasks.register("publishOmsApi") {
    group = "publishing"
    description =
        "Runs tests, checks remote version, publishes oms-api to local Maven (GitHub Pages) and updates index.html"

    dependsOn(
        "jvmKotest",
        "checkRemoteVersionNotPublished",
        "publishOmsApiPublicationToOperateMyServerPagesRepository"
    )

    finalizedBy(generateIndexHtmlTask)
}