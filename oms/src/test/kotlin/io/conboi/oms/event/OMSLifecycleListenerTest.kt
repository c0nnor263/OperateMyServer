package io.conboi.oms.event

import io.conboi.oms.api.OMSFeatureManagers
import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.foundation.feature.FeatureManager
import io.conboi.oms.api.foundation.reason.StopReason
import io.conboi.oms.api.infrastructure.file.OMSRootPath
import io.conboi.oms.content.StopManager
import io.kotest.core.spec.style.FunSpec
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import io.mockk.verifyOrder
import net.minecraft.server.MinecraftServer
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

class OMSLifecycleListenerTest : FunSpec({

    val server = mockk<MinecraftServer>(relaxed = true)
    val featureManager = mockk<FeatureManager>(relaxed = true)

    beforeTest {
        mockkObject(FORGE_BUS)
        mockkObject(StopManager)
        mockkObject(OMSRootPath)
        mockkObject(OMSFeatureManagers)

        every { FORGE_BUS.post(any()) } returns true
        every { StopManager.installHook() } just Runs
        every { OMSRootPath.init(server) } just Runs
        every { OMSFeatureManagers.runForEach(any()) } answers {
            firstArg<(FeatureManager) -> Unit>().invoke(featureManager)
        }
    }

    afterTest {
        unmockkAll()
    }

    test("onRegisterFeaturesEvent should post public event and freeze feature managers") {
        OMSLifecycleListener.onRegisterFeaturesEvent(OMSLifecycleInternal.Feature.RegisterEvent())

        verifyOrder {
            FORGE_BUS.post(match { it is OMSLifecycle.Feature.RegisterEvent })
            featureManager.freeze()
        }
    }

    test("onServerReadyEvent should initialize paths, post config event and call onStartingEvent") {
        val event = OMSLifecycleInternal.Server.ReadyEvent(server)

        OMSLifecycleListener.onServerReadyEvent(event)

        verifyOrder {
            StopManager.installHook()
            OMSRootPath.init(server)
            FORGE_BUS.post(match { it is OMSLifecycle.Feature.RegisterConfigEvent })
            featureManager.onStartingEvent(match { it.server == server })
            FORGE_BUS.post(match { it is OMSLifecycle.StartingEvent && it.server == server })
        }
    }

    test("onTickingEvent should call onTickingEvent for all feature managers") {
        val event = OMSLifecycle.TickingEvent(server, isServerStopping = false)

        OMSLifecycleListener.onTickingEvent(event)

        verify { featureManager.onTickingEvent(event) }
    }

    test("onServerPreShutdownEvent should post StoppingEvent and call onStoppingEvent") {
        val event = OMSLifecycleInternal.Server.PreShutdownEvent(server)

        OMSLifecycleListener.onServerPreShutdownEvent(event)

        verifyOrder {
            FORGE_BUS.post(match { it is OMSLifecycle.StoppingEvent && it.server == server })
            featureManager.onStoppingEvent(match { it.server == server })
        }
    }

    test("onStopRequestedEvent should delegate to StopManager") {
        val reason = mockk<StopReason>()
        val event = OMSLifecycle.StopRequestedEvent(server, reason)

        every { StopManager.stop(event) } just Runs

        OMSLifecycleListener.onStopRequestedEvent(event)

        verify { StopManager.stop(event) }
    }
})