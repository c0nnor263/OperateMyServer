package plugins.extension

import javax.inject.Inject
import org.gradle.api.Action
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
}
