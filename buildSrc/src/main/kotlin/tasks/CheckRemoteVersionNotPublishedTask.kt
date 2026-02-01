package tasks

import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

abstract class CheckRemoteVersionNotPublishedTask @Inject constructor() : DefaultTask() {

    @get:Input
    abstract val groupId: Property<String>

    @get:Input
    abstract val artifactId: Property<String>

    @get:Input
    abstract val version: Property<String>

    @get:Input
    abstract val baseUrl: Property<String>

    override fun getGroup() = "verification"
    override fun getDescription() = "Checks that the current version is not already published remotely at GitHub Pages."

    @TaskAction
    fun check() {
        val groupPath = groupId.get().replace('.', '/')
        val versionPath = "$groupPath/${artifactId.get()}/${version.get()}"
        val fileName = "${artifactId.get()}-${version.get()}.pom"
        val fullUrl = "${baseUrl.get().trimEnd('/')}/$versionPath/$fileName"

        val force = project.findProperty("forcePublish") == "true"
        val exists = try {
            val connection = URL(fullUrl).openConnection() as HttpURLConnection
            connection.requestMethod = "HEAD"
            connection.connectTimeout = 3000
            connection.readTimeout = 3000
            connection.responseCode == HttpURLConnection.HTTP_OK
        } catch (e: Exception) {
            logger.warn("Could not connect to $fullUrl. Assuming not published. Reason: ${e.message}")
            false
        }

        if (exists) {
            if (force) {
                logger.warn("Remote version already exists at $fullUrl, but continuing due to --forcePublish.")
            } else {
                throw IllegalStateException(
                    "Remote version already exists at:\n$fullUrl\n\n" +
                            "Use `--forcePublish` to override this check."
                )
            }
        } else {
            logger.lifecycle("Remote version does not exist. Safe to publish.")
        }
    }
}
