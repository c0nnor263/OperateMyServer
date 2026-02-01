package io.conboi.oms.event.listener

import io.conboi.oms.OmsAddons
import io.conboi.oms.OperateMyServerAddon
import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.foundation.addon.OmsAddon
import io.conboi.oms.content.StopManager
import io.conboi.oms.event.OMSInternal
import io.conboi.oms.infrastructure.OmsStartLogger
import io.conboi.oms.infrastructure.file.OMSRootPath
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.slot
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
        mockkConstructor(OmsStartLogger::class)
    }

    beforeEach {
        every { FORGE_BUS.post(any()) } returns true

        every { StopManager.installHook() } just Runs
        every { OMSRootPath.init(mockServer) } just Runs
        every { OMSRootPath.root } returns mockk(relaxed = true)

        every { OmsAddons.onPrepare(any()) } just Runs
        every { OmsAddons.onServerReady(any()) } just Runs

        every { OmsAddons.onOmsStarted(any()) } just Runs
        every { OmsAddons.onOmsTick(any()) } just Runs
        every { OmsAddons.onOmsStopping(any()) } just Runs

        every { anyConstructed<OmsStartLogger>().showGreetings() } just Runs
    }

    afterEach {
        clearAllMocks()
    }

    context("onAddonPrepareEvent") {

        should("post Addon.RegisterEvent and call OmsAddons.onPrepare") {
            val prepareSlot = slot<List<OmsAddon>>()
            val registerEventSlot = slot<OMSLifecycle.Addon.RegisterEvent>()

            every { FORGE_BUS.post(capture(registerEventSlot)) } returns true
            every { OmsAddons.onPrepare(capture(prepareSlot)) } just Runs

            OMSLifecycleListener.onAddonPrepareEvent(
                OMSInternal.Addon.PrepareEvent()
            )

            registerEventSlot.isCaptured shouldBe true
            prepareSlot.isCaptured shouldBe true
            prepareSlot.captured.size shouldBe 0
        }
    }

    context("onServerReadyEvent") {

        should("delegate server ready lifecycle correctly") {
            val event = OMSInternal.Server.ReadyEvent(mockServer)

            OMSLifecycleListener.onServerReadyEvent(event)

            verifyOrder {
                OmsAddons.onServerReady(mockServer)
                OmsAddons.onOmsStarted(
                    match { it.server == mockServer }
                )
                FORGE_BUS.post(
                    match { it is OMSLifecycle.StartingEvent && it.server == mockServer }
                )
            }
        }
    }

    context("onTickingEvent") {

        should("forward ticking event to OmsAddons") {
            val event = OMSLifecycle.TickingEvent(
                server = mockServer,
                isServerStopping = false
            )

            OMSLifecycleListener.onTickingEvent(event)

            verify { OmsAddons.onOmsTick(event) }
        }
    }

    context("onServerPreShutdownEvent") {

        should("post stopping event and notify addons") {
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
    }

    context("onAddonRegisterEvent") {

        should("register OperateMyServerAddon") {
            var captured: OmsAddon? = null

            val event = OMSLifecycle.Addon.RegisterEvent { addon ->
                captured = addon
            }

            OMSLifecycleListener.onAddonRegisterEvent(event)

            captured shouldNotBe null
            captured.shouldBeInstanceOf<OperateMyServerAddon>()
        }
    }

})
