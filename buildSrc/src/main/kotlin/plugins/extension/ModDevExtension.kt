package plugins.extension

import javax.inject.Inject
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested

abstract class ModDevExtension @Inject constructor(objects: ObjectFactory) {

    val includeTest: Property<Boolean> =
        objects.property(Boolean::class.java).convention(false)

    internal var setupModConfigured: Boolean = false
    internal val setupModListeners = mutableListOf<() -> Unit>()

    @get:Nested
    val setupMod: ModDevSetupModExtension =
        objects.newInstance(ModDevSetupModExtension::class.java)

    fun setupMod(action: Action<ModDevSetupModExtension>) {
        action.execute(setupMod)
        setupModConfigured = true
        setupModListeners.forEach { it() }
    }

    internal fun onSetupModConfigured(listener: () -> Unit) {
        setupModListeners += listener
        if (setupModConfigured) {
            listener()
        }
    }

    @get:Nested
    val dependentProjects: ListProperty<String> =
        objects.listProperty(String::class.java).convention(emptyList())

    fun dependsOn(vararg projects: String) {
        dependentProjects.addAll(projects.toList())
    }

    @get:Nested
    val toml = objects.newInstance(ModDevTomlExtension::class.java)

    fun toml(action: Action<ModDevTomlExtension>) {
        action.execute(toml)
    }
}