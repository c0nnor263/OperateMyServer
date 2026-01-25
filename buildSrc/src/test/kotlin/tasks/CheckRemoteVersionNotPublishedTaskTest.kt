package tasks

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.string.shouldContain
import io.mockk.clearAllMocks
import org.gradle.testfixtures.ProjectBuilder

class CheckRemoteVersionNotPublishedTaskTest : ShouldSpec({

    lateinit var sut: CheckRemoteVersionNotPublishedTask

    beforeEach {
        val project = ProjectBuilder.builder().build()
        sut = project.tasks.register("checkRemote", CheckRemoteVersionNotPublishedTask::class.java).get()
    }

    afterEach {
        clearAllMocks()
    }

    context("when version does not exist remotely") {
        should("pass without exception") {
            sut.groupId.set("com.fake")
            sut.artifactId.set("nonexistent-artifact")
            sut.version.set("0.0.0-dev")
            sut.baseUrl.set("https://repo1.maven.org/maven2/")

            sut.check()
        }
    }

    context("when version exists remotely") {
        should("throw exception") {
            sut.groupId.set("org.apache.commons")
            sut.artifactId.set("commons-lang3")
            sut.version.set("3.12.0")
            sut.baseUrl.set("https://repo1.maven.org/maven2/")

            shouldThrow<IllegalStateException> {
                sut.check()
            }.message shouldContain "Remote version already exists"
        }

        should("allow with forcePublish=true") {
            val project = ProjectBuilder.builder().build()
            project.extensions.extraProperties["forcePublish"] = "true"

            sut = project.tasks.create("checkRemote", CheckRemoteVersionNotPublishedTask::class.java).apply {
                groupId.set("org.apache.commons")
                artifactId.set("commons-lang3")
                version.set("3.12.0")
                baseUrl.set("https://repo1.maven.org/maven2/")
            }

            sut.check() // no exception
        }
    }

    context("when remote request fails") {
        should("assume not published") {
            val fakeUrl = "https://example.invalid/404"
            sut.groupId.set("some")
            sut.artifactId.set("thing")
            sut.version.set("123")
            sut.baseUrl.set(fakeUrl)

            sut.check() // should silently continue
        }
    }
})
