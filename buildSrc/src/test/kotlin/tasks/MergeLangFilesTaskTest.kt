package tasks

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.maps.shouldContainExactly
import java.io.File
import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.kotlin.com.google.gson.Gson

class MergeLangFilesTaskTest : ShouldSpec({

    lateinit var sut: MergeLangFilesTask
    lateinit var outputDir: File
    lateinit var mainLangDir: File
    lateinit var subprojectDir: File
    val gson = Gson()

    beforeEach {
        val rootProject = ProjectBuilder.builder().withName("root").build()
        val subProject = ProjectBuilder.builder()
            .withParent(rootProject)
            .withName("sub")
            .build()

        mainLangDir = File(rootProject.projectDir, "src/main/resources/assets/mymod/lang").apply { mkdirs() }
        subprojectDir = File(subProject.projectDir, "src/main/resources/assets/mymod/lang").apply { mkdirs() }

        File(mainLangDir, "en_us.json").writeText("""{ "key.root": "Root translation" }""")
        File(subprojectDir, "en_us.json").writeText("""{ "key.sub": "Sub translation" }""")
        File(subprojectDir, "ru_ru.json").writeText("""{ "key.sub": "Перевод саба" }""")

        outputDir = File(rootProject.buildDir, "lang-merged-test").apply { deleteRecursively(); mkdirs() }

        sut = rootProject.tasks.register("mergeLang", MergeLangFilesTask::class.java).get()
        sut.modId.set("mymod")
        sut.outputDir.set(outputDir)
        sut.projectPaths.set(listOf(":sub"))
    }

    should("merge lang files from root and subproject") {
        sut.merge()

        val mergedEn = File(outputDir, "en_us.json")
        val mergedRu = File(outputDir, "ru_ru.json")

        mergedEn.shouldExist()
        mergedRu.shouldExist()

        val enContent = gson.fromJson(mergedEn.readText(), Map::class.java) as Map<String, String>
        val ruContent = gson.fromJson(mergedRu.readText(), Map::class.java) as Map<String, String>

        enContent.shouldContainExactly(
            mapOf(
                "key.root" to "Root translation",
                "key.sub" to "Sub translation"
            )
        )

        ruContent.shouldContainExactly(
            mapOf("key.sub" to "Перевод саба")
        )
    }

    should("handle missing lang directory gracefully") {
        val project = ProjectBuilder.builder().withName("ghost").build()
        val task = project.tasks.create("mergeLang", MergeLangFilesTask::class.java)
        task.modId.set("ghostmod")
        task.outputDir.set(File(project.buildDir, "lang-missing-test"))
        task.projectPaths.set(emptyList())

        task.merge() // should not throw
    }
})
