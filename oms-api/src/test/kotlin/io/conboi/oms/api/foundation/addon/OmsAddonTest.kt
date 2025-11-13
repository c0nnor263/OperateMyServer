package io.conboi.oms.api.foundation.addon

import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.foundation.feature.OmsFeature
import io.conboi.oms.api.foundation.file.AddonPaths
import io.conboi.oms.api.foundation.logging.OMSLogger
import io.conboi.oms.api.foundation.manager.FeatureManager
import io.conboi.oms.api.foundation.manager.FeatureManagerInfo
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.nio.file.Path

class OmsAddonTest : ShouldSpec({

    lateinit var sut: OmsAddon
    val mockFeatureManager: FeatureManager = mockk(relaxed = true)
    val mockFeatureManagerInfo = mockk<FeatureManagerInfo>()
    val mockPaths: AddonPaths = mockk(relaxed = true)

    val mockFeature1: OmsFeature<*> = mockk(relaxed = true)
    val mockFeature2: OmsFeature<*> = mockk(relaxed = true)

    beforeEach {
        every { mockFeatureManager.info() } returns mockFeatureManagerInfo
        sut = object : OmsAddon("testaddon") {
            override val featureManager: FeatureManager = mockFeatureManager
            override val paths: AddonPaths = mockPaths
            override val logger: OMSLogger = OMSLogger
        }
    }

    afterEach {
        clearAllMocks()
    }

    context("constructor") {

        should("accept valid id") {
            val addon = object : OmsAddon("valid_id-1") {}
            addon.id shouldBe "valid_id-1"
        }

        should("reject invalid id") {
            val ex = shouldThrow<IllegalStateException> {
                object : OmsAddon("InvalidID!") {}
            }
            ex.message shouldBe "Invalid addon id: InvalidID!. It must only contain lowercase letters, numbers, underscores, and hyphens."
        }
    }

    context("info") {

        should("return addon info with featureManager info") {
            val info = sut.info()
            info.id shouldBe "testaddon"
            info.featureManagerInfo shouldBe mockFeatureManagerInfo
        }
    }

    context("onInitializeOmsRoot") {

        should("delegate to paths.onInitializeOmsRoot") {
            val root = mockk<Path>()
            sut.onInitializeOmsRoot(root)
            verify { mockPaths.onInitializeOmsRoot(root) }
        }
    }

    context("onRegisterFeatures") {

        should("register provided features in featureManager") {
            sut.onRegisterFeatures(listOf(mockFeature1, mockFeature2))

            verify { mockFeatureManager.register(mockFeature1) }
            verify { mockFeatureManager.register(mockFeature2) }
        }

        should("do nothing when feature list is empty") {
            sut.onRegisterFeatures(emptyList())

            verify(exactly = 0) { mockFeatureManager.register(any()) }
        }
    }

    context("onRegisterConfigs") {

        should("delegate to featureManager.onRegisterConfig") {
            sut.onRegisterConfigs()
            verify { mockFeatureManager.onRegisterConfig() }
        }
    }

    context("onFreeze") {

        should("call featureManager.freeze") {
            sut.onFreeze()
            verify { mockFeatureManager.freeze() }
        }
    }

    context("lifecycle events") {

        should("call onOmsStarted on featureManager") {
            val event = mockk<OMSLifecycle.StartingEvent>()
            sut.onOmsStarted(event)

            verify { mockFeatureManager.onStartingEvent(event) }
        }

        should("call onOmsTick on featureManager") {
            val event = mockk<OMSLifecycle.TickingEvent>()
            sut.onOmsTick(event)

            verify { mockFeatureManager.onTickingEvent(event) }
        }

        should("call onOmsStopping on featureManager") {
            val event = mockk<OMSLifecycle.StoppingEvent>()
            sut.onOmsStopping(event)

            verify { mockFeatureManager.onStoppingEvent(event) }
        }
    }

    context("getFeatureById") {

        should("delegate to featureManager.getFeatureById") {
            every { mockFeatureManager.getFeatureById<OmsFeature<*>>("f1") } returns mockFeature1

            sut.getFeatureById<OmsFeature<*>>("f1") shouldBe mockFeature1
        }

        should("return null when feature not found") {
            every { mockFeatureManager.getFeatureById<OmsFeature<*>>("missing") } returns null

            sut.getFeatureById<OmsFeature<*>>("missing") shouldBe null
        }
    }
})

