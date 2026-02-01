package plugins.extension

import javax.inject.Inject
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

abstract class ModDevSetupModExtension @Inject constructor(objects: ObjectFactory) {
    val id: Property<String> =
        objects.property(String::class.java)

    val version: Property<String> =
        objects.property(String::class.java)

    val displayName: Property<String> =
        objects.property(String::class.java)

    val authors: Property<String> =
        objects.property(String::class.java)

    val description: Property<String> =
        objects.property(String::class.java)

    val license: Property<String> =
        objects.property(String::class.java)

    val groupId: Property<String> =
        objects.property(String::class.java)
}