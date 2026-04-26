package plugins.extension

import javax.inject.Inject
import org.gradle.api.Named
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

abstract class ModDependencyDeclaration @Inject constructor(
    private val dependencyName: String,
    objects: ObjectFactory
) : Named, ModDependencySpec {

    override fun getName(): String = dependencyName

    override val modId: Property<String> = objects.property(String::class.java)
    override val mandatory: Property<Boolean> = objects.property(Boolean::class.java)
    override val versionRange: Property<String> = objects.property(String::class.java)
    override val ordering: Property<String> = objects.property(String::class.java)
    override val side: Property<String> = objects.property(String::class.java)

    init {
        modId.convention(dependencyName)
        mandatory.convention(true)
        ordering.convention("NONE")
        side.convention("SERVER")
    }
}