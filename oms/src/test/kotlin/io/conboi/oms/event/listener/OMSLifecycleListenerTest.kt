package io.conboi.oms.event.listener

import io.conboi.oms.api.OmsAddons
import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.content.StopManager
import io.conboi.oms.event.OMSInternal
import io.conboi.oms.infrastructure.file.OMSRootPath
import io.kotest.core.spec.style.ShouldSpec
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import io.mockk.verifyOrder
import net.minecraft.server.MinecraftServer
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

class OMSLifecycleListenerTest : ShouldSpec({

    val mockServer = mockk<MinecraftServer>(relaxed = true)

    beforeSpec {
        mockkObject(FORGE_BUS)
        mockkObject(StopManager)
        mockkObject(OMSRootPath)
        mockkObject(OmsAddons)
    }

    beforeEach {
        every { FORGE_BUS.post(any()) } returns true

        every { StopManager.installHook() } just Runs
        every { OMSRootPath.init(mockServer) } just Runs
        every { OMSRootPath.root } returns mockk()

        every { OmsAddons.onRegisterFeatures() } just Runs
        every { OmsAddons.freeze() } just Runs
        every { OmsAddons.onInitializeOmsRoot(any()) } just Runs
        every { OmsAddons.onRegisterConfigs() } just Runs

        every { OmsAddons.onOmsStarted(any()) } just Runs
        every { OmsAddons.onOmsTick(any()) } just Runs
        every { OmsAddons.onOmsStopping(any()) } just Runs
    }

    afterSpec {
        clearAllMocks()
    }

    context("onFeaturePrepareEvent") {
        should("register features and freeze after") {
            OMSLifecycleListener.onFeaturePrepareEvent(OMSInternal.Feature.PrepareEvent())

            verifyOrder {
                OmsAddons.onRegisterFeatures()
                OmsAddons.freeze()
            }
        }
    }

    context("onServerReadyEvent") {
        should("install hook, init paths, register configs, start OMS") {
            every { OMSRootPath.root } returns mockk(relaxed = true)
            every { OmsAddons.info() } returns mockk(relaxed = true)

            val event = OMSInternal.Server.ReadyEvent(mockServer)

            OMSLifecycleListener.onServerReadyEvent(event)

            verify(exactly = 1) { StopManager.installHook() }
            verify(exactly = 1) { OMSRootPath.init(mockServer) }
            verify(exactly = 1) { OmsAddons.onInitializeOmsRoot(any()) }
            verify(exactly = 1) { OmsAddons.onRegisterConfigs() }
            verify(exactly = 1) { OmsAddons.onOmsStarted(any()) }
            verify(exactly = 1) { FORGE_BUS.post(any()) }
        }
    }


    context("onTickingEvent") {
        should("call OmsAddons.onOmsTick") {
            val event = OMSLifecycle.TickingEvent(mockServer, isServerStopping = false)

            OMSLifecycleListener.onTickingEvent(event)

            verify { OmsAddons.onOmsTick(event) }
        }
    }

    should("post StoppingEvent and notify addons") {
        val event = OMSInternal.Server.PreShutdownEvent(mockServer)

        OMSLifecycleListener.onServerPreShutdownEvent(event)

        verifyOrder {
            FORGE_BUS.post(
                match { it is OMSLifecycle.StoppingEvent && it.server == mockServer }
            )

            OmsAddons.onOmsStopping(
                match { it.server == mockServer }
            )
        }
    }
})
