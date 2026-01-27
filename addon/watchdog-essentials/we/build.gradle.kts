plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.benManesVersions)
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
        id.set(WatchdogEssentials.ID)
        version.set(WatchdogEssentials.VERSION)
        displayName.set(WatchdogEssentials.DISPLAY_NAME)
        description.set(WatchdogEssentials.DESCRIPTION)
        groupId.set(WatchdogEssentials.GROUP_ID)
        license.set(WatchdogEssentials.LICENSE)
        authors.set(WatchdogEssentials.AUTHOR)
    }

    dependsOn(
        ":addon:watchdog-essentials:common",
        ":addon:watchdog-essentials:addon:empty-server-restart",
        ":addon:watchdog-essentials:addon:low-tps",
    )
}

dependencies {
    modImplementation(projects.oms)
    implementation("io.conboi.oms:oms-api:1.0.0")
    implementation(projects.omsCommon)

    implementation(libs.kotlinforforge)

    testImplementation(projects.omsTesting)
}

//
//publisher {
//    apiKeys {
//        curseforge(System.getenv("CURSE_FORGE_API_KEY"))
//    }
//
//    curseID.set("1341025")
//    versionType.set("release")
//    changelog.set(file("CHANGELOG.md"))
//    version.set(project.version.toString())
//    displayName.set("$modDisplayName $modVersion")
//    setGameVersions(libs.versions.minecraft.get())
//    setLoaders(ModLoader.FORGE, ModLoader.NEOFORGE)
//    setCurseEnvironment(CurseEnvironment.SERVER)
//    artifact.set("build/libs/${base.archivesName.get()}-${project.version}.jar")
//
//    curseDepends {
//        required("kotlin-for-forge")
//    }
//}