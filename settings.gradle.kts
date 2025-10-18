enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        maven("https://maven.minecraftforge.net/")
        maven("https://maven.firstdark.dev/releases")
        gradlePluginPortal()
    }
}
plugins {
    // This plugin allows Gradle to automatically download arbitrary versions of Java for you
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

include(":oms")
include(":oms-api")
include(":oms-core")
include(":addon:bundled:scheduled-restart")
include(":addon:bundled:low-tps")
include(":addon:bundled:empty-server-restart")

rootProject.name = "operate-my-server"