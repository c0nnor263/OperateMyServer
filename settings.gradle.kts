enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        maven("https://maven.minecraftforge.net/")
        maven("https://maven.firstdark.dev/releases")
        gradlePluginPortal()
    }
}

include(":common")
include(":feature:autorestart")
include(":feature:lowtps")

rootProject.name = "operate-my-server"
