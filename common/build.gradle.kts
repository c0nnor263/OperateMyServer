plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.modDevGradle)
    alias(libs.plugins.benManesVersions)
    alias(libs.plugins.kotlinxSerialization)
    idea
}

apply<ModDevPlugin>()

legacyForge {
    version = libs.versions.forge.get()

    parchment {
        mappingsVersion = libs.versions.parchment.get()
        minecraftVersion = libs.versions.minecraft.get()
    }
}

dependencies {
    // Kotlin For Forge
    implementation(libs.kotlinforforge)

    implementation(libs.kotlinxSerialization)


    implementation(jarJar("io.github.llamalad7:mixinextras-forge:0.4.1")!!)
    compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:${libs.versions.mixin.get()}")!!)

    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
}