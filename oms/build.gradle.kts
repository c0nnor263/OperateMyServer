import com.hypherionmc.modpublisher.properties.CurseEnvironment
import com.hypherionmc.modpublisher.properties.ModLoader
import plugins.modDisplayName
import plugins.modVersion

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
}

modDev {
    includeTest.set(true)

    setupMod {
        id.set(OperateMyServer.ID)
        version.set(OperateMyServer.VERSION)
        displayName.set(OperateMyServer.DISPLAY_NAME)
        description.set(OperateMyServer.DESCRIPTION)
        groupId.set(OperateMyServer.GROUP_ID)
        license.set(OperateMyServer.LICENSE)
        authors.set(OperateMyServer.AUTHOR)
    }

    dependsOn(
        ":oms-api",
        ":oms-common",
        ":addon:bundled:scheduled-restart"
    )
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
    changelog.set(file("CHANGELOG.md"))
    version.set(project.version.toString())
    displayName.set("$modDisplayName $modVersion")
    setGameVersions(libs.versions.minecraft.get())
    setLoaders(ModLoader.FORGE, ModLoader.NEOFORGE)
    setCurseEnvironment(CurseEnvironment.SERVER)
    artifact.set("build/libs/${base.archivesName.get()}-${project.version}.jar")

    curseDepends {
        required("kotlin-for-forge")
    }
}