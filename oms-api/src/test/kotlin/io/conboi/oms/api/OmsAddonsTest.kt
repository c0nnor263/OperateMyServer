package io.conboi.oms.api

import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.foundation.addon.OmsAddon
import io.conboi.oms.api.foundation.addon.OmsAddonInfo
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.nio.file.Path
import net.minecraft.server.MinecraftServer

class OmsAddonsTest : ShouldSpec({

    fun clearRegistry() {
        val field = OmsAddons::class.java.getDeclaredField("registry")
        field.isAccessible = true
        val map = field.get(OmsAddons) as MutableMap<*, *>
        map.clear()

        val frozenField = OmsAddons::class.java.getDeclaredField("frozen")
        frozenField.isAccessible = true
        frozenField.setBoolean(OmsAddons, false)
    }

    val mockServer = mockk<MinecraftServer>(relaxed = true)
    val mockAddonA = mockk<OmsAddon>(relaxed = true)
    val mockAddonB = mockk<OmsAddon>(relaxed = true)
    val mockAddonInfoA = mockk<OmsAddonInfo>(relaxed = true)
    val mockAddonInfoB = mockk<OmsAddonInfo>(relaxed = true)

    beforeEach {
        clearRegistry()
        every { mockAddonA.id } returns "addonA"
        every { mockAddonB.id } returns "addonB"
        every { mockAddonA.info() } returns mockAddonInfoA
        every { mockAddonB.info() } returns mockAddonInfoB
    }

    context("register") {

        should("register addon") {
            OmsAddons.register(mockAddonA)
            OmsAddons.get("addonA") shouldBe mockAddonA
        }

        should("not allow duplicate addon id") {
            OmsAddons.register(mockAddonA)

            val ex = shouldThrow<IllegalArgumentException> {
                OmsAddons.register(mockAddonA)
            }

            ex.message shouldBe "Addon 'addonA' already registered"
        }

        should("not allow registering after freeze") {
            OmsAddons.freeze()

            val ex = shouldThrow<IllegalStateException> {
                OmsAddons.register(mockAddonA)
            }

            ex.message shouldBe "OmsAddons.register(addonA) called after freeze. Register addons only in @Mod init."
        }
    }

    context("get") {

        should("return addon by id") {
            OmsAddons.register(mockAddonA)
            OmsAddons.get("addonA") shouldBe mockAddonA
        }

        should("return null for missing id") {
            OmsAddons.get("missing") shouldBe null
        }
    }

    context("info") {

        should("return info for all registered addons") {
            OmsAddons.register(mockAddonA)
            OmsAddons.register(mockAddonB)

            val info = OmsAddons.info()
            info.addonsInfo.shouldContainExactly(listOf(mockAddonInfoA, mockAddonInfoB))
        }
    }

    context("onInitializeOmsRoot") {
        should("call onInitializeOmsRoot for all addons") {
            OmsAddons.register(mockAddonA)
            OmsAddons.register(mockAddonB)

            val path = mockk<Path>()

            OmsAddons.onInitializeOmsRoot(path)

            verify { mockAddonA.onInitializeOmsRoot(path) }
            verify { mockAddonB.onInitializeOmsRoot(path) }
        }
    }

    context("onRegisterConfigs") {
        should("call onRegisterConfigs for all addons") {
            OmsAddons.register(mockAddonA)
            OmsAddons.register(mockAddonB)

            OmsAddons.onRegisterConfigs()

            verify { mockAddonA.onRegisterConfigs() }
            verify { mockAddonB.onRegisterConfigs() }
        }
    }

    context("onRegisterFeatures") {
        should("call onRegisterFeatures only before freeze") {
            OmsAddons.register(mockAddonA)
            OmsAddons.register(mockAddonB)

            OmsAddons.onRegisterFeatures()
            verify { mockAddonA.onRegisterFeatures() }
            verify { mockAddonB.onRegisterFeatures() }

            clearMocks(mockAddonA, mockAddonB)
            OmsAddons.freeze()
            OmsAddons.onRegisterFeatures()

            verify(exactly = 0) { mockAddonA.onRegisterFeatures() }
            verify(exactly = 0) { mockAddonB.onRegisterFeatures() }
        }
    }

    context("onOmsStarted") {
        should("call onOmsStarted event for all addons") {
            OmsAddons.register(mockAddonA)
            OmsAddons.register(mockAddonB)

            val event = OMSLifecycle.StartingEvent(mockServer)
            OmsAddons.onOmsStarted(event)

            verify { mockAddonA.onOmsStarted(event) }
            verify { mockAddonB.onOmsStarted(event) }
        }
    }

    context("onOmsTick") {
        should("call onOmsTick event for all addons") {
            OmsAddons.register(mockAddonA)
            OmsAddons.register(mockAddonB)

            val event = OMSLifecycle.TickingEvent(mockk(relaxed = true), false)
            OmsAddons.onOmsTick(event)

            verify { mockAddonA.onOmsTick(event) }
            verify { mockAddonB.onOmsTick(event) }
        }
    }

    context("onOmsStopping") {
        should("call onOmsStopping event for all addons") {
            OmsAddons.register(mockAddonA)
            OmsAddons.register(mockAddonB)

            val event = OMSLifecycle.StoppingEvent(mockk(relaxed = true))
            OmsAddons.onOmsStopping(event)

            verify { mockAddonA.onOmsStopping(event) }
            verify { mockAddonB.onOmsStopping(event) }
        }
    }

    context("freeze") {

        should("freeze OmsAddons and call onFreeze on each addon") {
            OmsAddons.register(mockAddonA)
            OmsAddons.register(mockAddonB)

            OmsAddons.freeze()

            verify { mockAddonA.onFreeze() }
            verify { mockAddonB.onFreeze() }
        }

        should("not call onFreeze on second freeze") {
            OmsAddons.register(mockAddonA)

            OmsAddons.freeze()
            clearMocks(mockAddonA)

            OmsAddons.freeze()
            verify(exactly = 0) { mockAddonA.onFreeze() }
        }
    }

    context("forEachAddon") {

        should("iterate over addons") {
            OmsAddons.register(mockAddonA)
            OmsAddons.register(mockAddonB)

            val called = mutableListOf<String>()

            OmsAddons.forEachAddon {
                called += it.id
            }

            called.shouldContainExactly(listOf("addonA", "addonB"))
        }
    }
})
