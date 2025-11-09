package io.conboi.oms.api.foundation.feature

import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.foundation.TickTimer
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class FeatureManagerTest : FunSpec({

    lateinit var manager: FeatureManager
    lateinit var feature1: OmsFeature<*>
    lateinit var feature2: OmsFeature<*>

    beforeTest {
        manager = object : FeatureManager() {
            override val modId: String = "testmod"
            override val name: String = "testmanager"
        }

        feature1 = mockk(relaxed = true)
        every { feature1.info } returns FeatureInfo("feature1", FeatureInfo.Priority.NONE)

        feature2 = mockk(relaxed = true)
        every { feature2.info } returns FeatureInfo("feature2", FeatureInfo.Priority.CRITICAL)
    }

    context("register") {
        test("should register feature successfully") {
            manager.register(feature1)
            manager.getFeatureById<OmsFeature<*>>("feature1") shouldBe feature1
        }

        test("should throw if registering duplicate feature") {
            manager.register(feature1)
            val ex = shouldThrow<IllegalArgumentException> {
                manager.register(feature1)
            }
            ex.message shouldBe "Feature 'feature1' already registered"
        }

        test("should throw if registering after freeze") {
            manager.freeze()
            val ex = shouldThrow<IllegalStateException> {
                manager.register(feature1)
            }
            ex.message shouldBe "Cannot register features after server has started!"
        }
    }

    context("freeze") {
        test("should sort features by priority when frozen") {
            manager.register(feature1)
            manager.register(feature2)
            manager.freeze()

            manager.prioritizedFeatures.map { it.info.id } shouldContainExactly listOf("feature2", "feature1")
        }

        test("should not recompute priorities if frozen twice") {
            manager.register(feature1)
            manager.register(feature2)
            manager.freeze()
            val original = manager.prioritizedFeatures
            manager.freeze()
            manager.prioritizedFeatures shouldBe original
        }
    }

    context("getFeatureById") {
        test("should return registered feature") {
            manager.register(feature1)
            manager.getFeatureById<OmsFeature<*>>("feature1") shouldBe feature1
        }
    }

    context("onStartingEvent") {
        test("should call onOmsStarted on all features") {
            manager.register(feature1)
            manager.register(feature2)
            manager.freeze()

            val event = mockk<OMSLifecycle.StartingEvent>()
            manager.onStartingEvent(event)

            verify { feature1.onOmsStarted(event) }
            verify { feature2.onOmsStarted(event) }
        }
    }

    context("onTickingEvent") {
        test("should call onOmsTick only for enabled features") {
            val event = mockk<OMSLifecycle.TickingEvent> {
                every { server.tickCount } returns 20
                every { isServerStopping } returns false
            }

            every { feature1.isEnabled() } returns false
            every { feature2.isEnabled() } returns true

            manager.register(feature1)
            manager.register(feature2)
            manager.freeze()

            manager.onTickingEvent(event)

            verify(exactly = 0) { feature1.onOmsTick(any()) }
            verify(exactly = 1) { feature2.onOmsTick(event) }
        }

        test("should not call anything if server is stopping") {
            val event = mockk<OMSLifecycle.TickingEvent> {
                every { server.tickCount } returns 20
                every { isServerStopping } returns true
            }

            every { feature1.isEnabled() } returns true
            manager.register(feature1)
            manager.freeze()

            manager.onTickingEvent(event)

            verify(exactly = 0) { feature1.onOmsTick(any()) }
        }

        test("should skip if tickTimer returns false") {
            val managerWithTick = object : FeatureManager() {
                override val modId = "testmod"
                override val tickTimer = mockk<TickTimer> {
                    every { shouldFire(0) } returns false
                }
            }

            val event = mockk<OMSLifecycle.TickingEvent> {
                every { server.tickCount } returns 0
                every { isServerStopping } returns false
            }

            managerWithTick.register(feature1)
            managerWithTick.freeze()
            managerWithTick.onTickingEvent(event)

            verify(exactly = 0) { feature1.onOmsTick(any()) }
        }
    }

    context("onStoppingEvent") {
        test("should call onOmsStopping and clear registry") {
            manager.register(feature1)
            manager.register(feature2)
            manager.freeze()

            val event = mockk<OMSLifecycle.StoppingEvent>()
            manager.onStoppingEvent(event)

            verify { feature1.onOmsStopping(event) }
            verify { feature2.onOmsStopping(event) }
            manager.getFeatureById<OmsFeature<*>>("feature1") shouldBe null
        }
    }

    context("getFullId") {
        test("should return full ID in format modId:featureId") {
            manager.register(feature1)
            val fullId = manager.getFullId()
            fullId shouldBe "testmod:testmanager"
        }
    }

    test("default name should be 'main' if not overridden") {
        val defaultManager = object : FeatureManager() {
            override val modId: String = "testmod"
        }
        defaultManager.name shouldBe "main"
        defaultManager.getFullId() shouldBe "testmod:main"
    }
})