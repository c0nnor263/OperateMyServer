package tasks

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import io.mockk.unmockkAll
import org.gradle.testfixtures.ProjectBuilder

class CheckRemoteVersionNotPublishedTaskTest : FunSpec({

    lateinit var task: CheckRemoteVersionNotPublishedTask

    beforeEach {
        val project = ProjectBuilder.builder().build()
        task = project.tasks.create("checkRemote", CheckRemoteVersionNotPublishedTask::class.java)
    }

    afterEach {
        unmockkAll()
    }

    context("when version does not exist remotely") {
        test("should pass without exception") {
            task.groupId.set("com.fake")
            task.artifactId.set("nonexistent-artifact")
            task.version.set("0.0.0-dev")
            task.baseUrl.set("https://repo1.maven.org/maven2/")

            task.check()
        }
    }

    context("when version exists remotely") {
        test("should throw exception") {
            task.groupId.set("org.apache.commons")
            task.artifactId.set("commons-lang3")
            task.version.set("3.12.0")
            task.baseUrl.set("https://repo1.maven.org/maven2/")

            shouldThrow<IllegalStateException> {
                task.check()
            }.message shouldContain "Remote version already exists"
        }

        test("should allow with forcePublish=true") {
            val project = ProjectBuilder.builder().build()
            project.extensions.extraProperties["forcePublish"] = "true"

            task = project.tasks.create("checkRemote", CheckRemoteVersionNotPublishedTask::class.java).apply {
                groupId.set("org.apache.commons")
                artifactId.set("commons-lang3")
                version.set("3.12.0")
                baseUrl.set("https://repo1.maven.org/maven2/")
            }

            task.check() // no exception
        }
    }

    context("when remote request fails") {
        test("should assume not published") {
            val fakeUrl = "https://example.invalid/404"
            task.groupId.set("some")
            task.artifactId.set("thing")
            task.version.set("123")
            task.baseUrl.set(fakeUrl)

            task.check() // should silently continue
        }
    }
})
