package io.conboi.oms.api.foundation.file

import io.conboi.oms.api.foundation.feature.FeatureInfo
import io.conboi.oms.api.infrastructure.file.OMSRootPath
import io.conboi.oms.api.infrastructure.file.OMSRootPath.ensure
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import java.nio.file.Path

class AddonPathsTest : FunSpec({

    lateinit var rootPath: Path
    lateinit var addonPath: Path
    lateinit var featurePath: Path

    beforeTest {
        mockkObject(OMSRootPath)

        // mock base path
        rootPath = mockk(relaxed = true)
        addonPath = mockk(relaxed = true)
        featurePath = mockk(relaxed = true)

        every { OMSRootPath.root } returns rootPath
        every { rootPath.ensure("myaddon") } returns addonPath
        every { addonPath.ensure("common") } returns addonPath
        every { addonPath.ensure("logs") } returns addonPath
        every { addonPath.ensure("cache") } returns addonPath

        every { addonPath.ensure("feature1") } returns featurePath
    }

    afterTest {
        unmockkAll()
    }

    test("should throw on invalid modId") {
        shouldThrow<IllegalArgumentException> {
            object : AddonPaths("Invalid@Mod!") {}
        }.message shouldBe "Invalid modId: Invalid@Mod!"
    }

    test("should return correct root paths") {
        val paths = object : AddonPaths("myaddon") {}

        paths.omsRoot shouldBe rootPath
        paths.addonRoot shouldBe addonPath
        paths.common shouldBe addonPath
        paths.logs shouldBe addonPath
        paths.cache shouldBe addonPath
    }

    test("should return correct feature path for AddonPathType") {
        val paths = object : AddonPaths("myaddon") {}

        val info = FeatureInfo("feature1")

        paths.forFeature(info, AddonPathType.COMMON) shouldBe featurePath
        paths.forFeature(info, AddonPathType.LOGS) shouldBe featurePath
        paths.forFeature(info, AddonPathType.CACHE) shouldBe featurePath
    }
})