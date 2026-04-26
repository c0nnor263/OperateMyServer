package plugins

import org.gradle.api.Project
import plugins.extension.ModDevExtension

private fun Project.modDevExt(): ModDevExtension =
    extensions.getByType(ModDevExtension::class.java)

val Project.modId: String
    get() = modDevExt().setupMod.id.get()

val Project.modVersion: String
    get() = modDevExt().setupMod.version.get()

val Project.modDisplayName: String
    get() = modDevExt().setupMod.displayName.get()

val Project.modDescription: String
    get() = modDevExt().setupMod.description.get()

val Project.modAuthors: String
    get() = modDevExt().setupMod.authors.get()

val Project.modLicense: String
    get() = modDevExt().setupMod.license.get()

val Project.modGroupId: String
    get() = modDevExt().setupMod.groupId.get()