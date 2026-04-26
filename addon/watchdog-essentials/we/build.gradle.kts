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
    dependsOn(
        projects.addon.watchdogEssentials.common.path,
        projects.addon.watchdogEssentials.feature.emptyServer.path,
        projects.addon.watchdogEssentials.feature.lowTps.path,
    )

    toml {
        dependency("oms") {
            mandatory.set(true)
            versionRange.set("[1.0.0,)")
        }
    }

    setupMod {
        id.set(WatchdogEssentials.ID)
        version.set(WatchdogEssentials.VERSION)
        displayName.set(WatchdogEssentials.DISPLAY_NAME)
        description.set(WatchdogEssentials.DESCRIPTION)
        groupId.set(WatchdogEssentials.GROUP_ID)
        license.set(WatchdogEssentials.LICENSE)
        authors.set(WatchdogEssentials.AUTHOR)
    }
}

dependencies {
    modLocalRuntime(projects.oms)
    modCompileOnly(libs.omsApi)
    implementation(projects.omsCommon)

    implementation(libs.kotlinforforge)
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