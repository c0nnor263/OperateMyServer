package plugins.extension

import javax.inject.Inject
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Nested

abstract class ModDevTomlExtension @Inject constructor(
    objects: ObjectFactory
) {
    @get:Nested
    val extras: MapProperty<String, Any> =
        objects.mapProperty(String::class.java, Any::class.java)

    fun extras(action: Action<MapProperty<String, Any>>) {
        action.execute(extras)
    }

    @get:Nested
    val dependencies: NamedDomainObjectContainer<ModDependencyDeclaration> =
        objects.domainObjectContainer(ModDependencyDeclaration::class.java)

    fun dependency(name: String, action: Action<ModDependencyDeclaration>) {
        val dependency = dependencies.maybeCreate(name)
        action.execute(dependency)
    }
}
