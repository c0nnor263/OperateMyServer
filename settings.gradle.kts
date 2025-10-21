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

// OMS
include(":oms")
include(":oms-core")
include(":oms-api")
include(":oms-utils")
include(":addon:bundled:scheduled-restart")

// Watchdog Essentials Addon
include(":addon:watchdog-essentials")
include(":addon:watchdog-essentials:core")
include(":addon:watchdog-essentials:addon:low-tps")
include(":addon:watchdog-essentials:addon:empty-server-restart")


rootProject.name = "operate-my-server"