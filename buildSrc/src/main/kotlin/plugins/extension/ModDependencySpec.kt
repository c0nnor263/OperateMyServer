package plugins.extension

import org.gradle.api.provider.Property

interface ModDependencySpec {
    val modId: Property<String>
    val mandatory: Property<Boolean>
    val versionRange: Property<String>
    val ordering: Property<String>
    val side: Property<String>
}