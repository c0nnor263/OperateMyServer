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
    testImplementation(libs.bundles.testing)
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

val generateIndexHtmlTask = tasks.register<GenerateIndexHtmlTask>("generateIndexHtml") {
    title.set("Index of /oms-api")
    outputDir.set(rootProject.projectDir.resolve("docs/oms-api"))
}

tasks.named("publishOmsApiPublicationToOperateMyServerDocsRepository") {
    finalizedBy(generateIndexHtmlTask)
}