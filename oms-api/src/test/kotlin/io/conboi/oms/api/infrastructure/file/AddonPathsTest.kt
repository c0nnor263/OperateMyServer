package io.conboi.oms.api.infrastructure.file

import io.conboi.oms.api.extension.ensure
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import java.nio.file.Path

class AddonPathsTest : ShouldSpec({
    lateinit var sut: AddonPaths
    val mockOmsRoot: Path = mockk(relaxed = true)
    val mockAddonRoot: Path = mockk(relaxed = true)
    val mockCommon: Path = mockk(relaxed = true)
    val mockLogs: Path = mockk(relaxed = true)
    val mockCache: Path = mockk(relaxed = true)
    val mockFeatureCommon: Path = mockk(relaxed = true)
    val mockFeatureLogs: Path = mockk(relaxed = true)
    val mockFeatureCache: Path = mockk(relaxed = true)

    beforeEach {
        mockkStatic(Path::ensure)

        every { mockOmsRoot.ensure("myaddon") } returns mockAddonRoot
        every { mockAddonRoot.ensure("common") } returns mockCommon
        every { mockAddonRoot.ensure("logs") } returns mockLogs
        every { mockAddonRoot.ensure("cache") } returns mockCache

        every { mockCommon.ensure("feature1") } returns mockFeatureCommon
        every { mockLogs.ensure("feature1") } returns mockFeatureLogs
        every { mockCache.ensure("feature1") } returns mockFeatureCache

        sut = object : AddonPaths("myaddon") {}
    }

    afterEach {
        clearAllMocks()
    }

    should("initialize omsRoot correctly with onInitializeOmsRoot") {
        sut.onInitializeOmsRoot(mockOmsRoot)

        sut.omsRoot shouldBe mockOmsRoot
    }

    should("build addonRoot and standard directories") {
        sut.onInitializeOmsRoot(mockOmsRoot)

        sut.addonRoot shouldBe mockAddonRoot
        sut.common shouldBe mockCommon
        sut.logs shouldBe mockLogs
        sut.cache shouldBe mockCache
    }

    should("return correct feature paths based on AddonPathType") {
        sut.onInitializeOmsRoot(mockOmsRoot)

        val featureId = "feature1"

        sut.forFeature(featureId, AddonPathType.COMMON) shouldBe mockFeatureCommon
        sut.forFeature(featureId, AddonPathType.LOGS) shouldBe mockFeatureLogs
        sut.forFeature(featureId, AddonPathType.CACHE) shouldBe mockFeatureCache
    }
})
