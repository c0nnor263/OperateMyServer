package io.conboi.oms.api.foundation.manager

import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.foundation.TickTimer
import io.conboi.oms.api.foundation.addon.AddonContext
import io.conboi.oms.api.foundation.feature.FeatureInfo
import io.conboi.oms.api.foundation.feature.OmsFeature
import io.conboi.oms.api.foundation.feature.Priority
import io.conboi.oms.api.infrastructure.config.FeatureConfig
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify

class FeatureManagerTest : ShouldSpec({

    lateinit var sut: FeatureManager
    lateinit var mockFeature1: OmsFeature<FeatureConfig>
    lateinit var mockFeature2: OmsFeature<FeatureConfig>
    val mockFeatureConfig1: FeatureConfig = mockk(relaxed = true)
    val mockFeatureConfig2: FeatureConfig = mockk(relaxed = true)
    val mockFeature1Info = mockk<FeatureInfo>(relaxed = true)
    val mockFeature2Info = mockk<FeatureInfo>(relaxed = true)
    val mockAddonContext = mockk<AddonContext>(relaxed = true)

    beforeEach {
        every { mockFeatureConfig1.name } returns "feature1"
        every { mockFeatureConfig2.name } returns "feature2"

        every { mockFeature1Info.id } returns "feature1"
        every { mockFeature2Info.id } returns "feature2"
        every { mockFeature1Info.priority } returns Priority.NONE
        every { mockFeature2Info.priority } returns Priority.CRITICAL

        mockFeature1 = spyk(object : OmsFeature<FeatureConfig>({ mockFeatureConfig1 }) {})
        mockFeature2 = spyk(object : OmsFeature<FeatureConfig>({ mockFeatureConfig2 }) {})
        mockFeature1.onOmsRegisterConfig()
        mockFeature2.onOmsRegisterConfig()

        every { mockFeature1.info() } returns mockFeature1Info
        every { mockFeature2.info() } returns mockFeature2Info


        sut = object : FeatureManager("testmod") {
            override val name = "testmanager"
        }

    }

    afterEach {
        clearAllMocks()
    }

    context("register") {

        should("register feature by config-provided id") {
            sut.register(mockFeature1)

            sut.getFeatureById<OmsFeature<*>>("feature1") shouldBe mockFeature1
        }

        should("throw when registering duplicate id") {
            sut.register(mockFeature1)

            val ex = shouldThrow<IllegalArgumentException> {
                sut.register(mockFeature1)
            }

            ex.message shouldBe "Feature 'feature1' already registered"
        }

        should("throw when registering after freeze") {
            sut.freeze()

            val ex = shouldThrow<IllegalStateException> {
                sut.register(mockFeature1)
            }

            ex.message shouldBe "Cannot register features after server has started!"
        }
    }

    context("freeze") {

        should("sort features by priority") {
            sut.register(mockFeature1)
            sut.register(mockFeature2)

            sut.freeze()

            sut.info().featuresInfo.map { it.id }
                .shouldContainExactly(listOf("feature2", "feature1"))
        }

        should("be idempotent") {
            sut.register(mockFeature1)
            sut.register(mockFeature2)
            sut.freeze()
            sut.freeze() // second call should do nothing

            sut.info().featuresInfo.map { it.id }
                .shouldContainExactly(listOf("feature2", "feature1"))
        }
    }

    context("getFeatureById") {

        should("return registered feature") {
            sut.register(mockFeature1)
            sut.getFeatureById<OmsFeature<*>>("feature1") shouldBe mockFeature1
        }
    }

    context("onStartingEvent") {

        should("call onOmsStarted on all features") {
            sut.register(mockFeature1)
            sut.register(mockFeature2)
            sut.freeze()

            val event = mockk<OMSLifecycle.StartingEvent>()

            sut.onStartingEvent(event, mockAddonContext)

            verify { mockFeature1.onOmsStarted(event, mockAddonContext) }
            verify { mockFeature2.onOmsStarted(event, mockAddonContext) }
        }
    }

    context("onTickingEvent") {

        should("tick only enabled features") {
            val event = mockk<OMSLifecycle.TickingEvent> {
                every { server.tickCount } returns 20
                every { isServerStopping } returns false
            }

            every { mockFeatureConfig1.isEnabled() } returns false
            every { mockFeatureConfig2.isEnabled() } returns true

            sut.register(mockFeature1)
            sut.register(mockFeature2)
            sut.freeze()

            sut.onTickingEvent(event, mockAddonContext)

            verify(exactly = 0) { mockFeature1.onOmsTick(any(), any()) }
            verify(exactly = 1) { mockFeature2.onOmsTick(event, mockAddonContext) }
        }

        should("not tick when server is stopping") {

            val event = mockk<OMSLifecycle.TickingEvent> {
                every { server.tickCount } returns 20
                every { isServerStopping } returns true
            }

            every { mockFeatureConfig1.isEnabled() } returns true

            sut.register(mockFeature1)
            sut.freeze()

            sut.onTickingEvent(event, mockAddonContext)

            verify(exactly = 0) { mockFeature1.onOmsTick(any(), any()) }
        }

        should("skip when tickTimer denies") {
            sut = object : FeatureManager("testmod") {
                override val tickTimer = mockk<TickTimer> {
                    every { shouldFire(0) } returns false
                }
                override val name = "x"
            }

            val event = mockk<OMSLifecycle.TickingEvent> {
                every { server.tickCount } returns 0
                every { isServerStopping } returns false
            }

            every { mockFeatureConfig1.isEnabled() } returns true

            mockFeature1.onOmsRegisterConfig()
            sut.register(mockFeature1)
            sut.freeze()
            sut.onTickingEvent(event, mockAddonContext)

            verify(exactly = 0) { mockFeature1.onOmsTick(any(), any()) }
        }
    }

    context("onStoppingEvent") {

        should("call onOmsStopping for all features and clear registry") {
            sut.register(mockFeature1)
            sut.register(mockFeature2)
            sut.freeze()

            val event = mockk<OMSLifecycle.StoppingEvent>()

            sut.onStoppingEvent(event, mockAddonContext)

            verify { mockFeature1.onOmsStopping(event, mockAddonContext) }
            verify { mockFeature2.onOmsStopping(event, mockAddonContext) }

            sut.getFeatureById<OmsFeature<*>>("feature1") shouldBe null
            sut.getFeatureById<OmsFeature<*>>("feature2") shouldBe null
        }
    }

    context("id") {
        should("return modId:name") {
            sut.id shouldBe "testmod:testmanager"
        }

        should("use default name when not overridden") {
            val manager = object : FeatureManager("abc") {}
            manager.id shouldBe "abc:main"
        }
    }

    context("onRegisterConfig") {
        should("call onOmsRegisterConfig for all features") {
            sut.register(mockFeature1)
            sut.register(mockFeature2)
            sut.freeze()

            sut.onRegisterConfig()

            verify { mockFeature1.onOmsRegisterConfig() }
            verify { mockFeature2.onOmsRegisterConfig() }
        }
    }

    context("features") {
        should("return features in priority order") {
            sut.register(mockFeature1)
            sut.register(mockFeature2)
            sut.freeze()

            val features = sut.features()

            features.shouldContainExactly(listOf(mockFeature2, mockFeature1))
        }
    }
})
