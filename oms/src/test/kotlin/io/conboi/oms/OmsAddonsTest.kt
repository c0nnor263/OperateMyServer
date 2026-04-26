package io.conboi.oms

import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.foundation.addon.AddonContext
import io.conboi.oms.api.foundation.addon.AddonContextSpec
import io.conboi.oms.api.foundation.addon.OmsAddon
import io.conboi.oms.api.foundation.feature.OmsFeature
import io.conboi.oms.api.foundation.manager.FeatureManager
import io.conboi.oms.api.infrastructure.file.AddonPaths
import io.conboi.oms.content.StopManager
import io.conboi.oms.foundation.addon.AddonInstance
import io.conboi.oms.foundation.addon.DefaultAddonContextFactory
import io.conboi.oms.infrastructure.file.OMSRootPath
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import io.mockk.verifySequence
import net.minecraft.server.MinecraftServer
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

class OmsAddonsTest : ShouldSpec({

    fun clearRegistry() {
        val field = OmsAddons::class.java.getDeclaredField("registry")
        field.isAccessible = true
        (field.get(OmsAddons) as MutableMap<*, *>).clear()
    }

    beforeSpec {
        mockkObject(DefaultAddonContextFactory)
        mockkObject(StopManager)
        mockkObject(OMSRootPath)
        mockkObject(FORGE_BUS)
    }

    beforeEach {
        clearRegistry()
        every { StopManager.installHook() } just Runs
        every { OMSRootPath.init(any()) } just Runs
        every { OMSRootPath.root } returns mockk(relaxed = true)
        every { FORGE_BUS.register(any()) } just Runs
    }

    afterEach {
        clearAllMocks()
    }

    fun addon(id: String, overrideId: String? = null): OmsAddon =
        mockk(relaxed = true) {
            every { this@mockk.id } returns id
            every { configureContext(any()) } answers {
                if (overrideId != null) AddonContextSpec(overrideId)
                else firstArg()
            }
        }

    fun context(
        id: String,
        fm: FeatureManager = mockk(relaxed = true),
        paths: AddonPaths = mockk(relaxed = true)
    ): AddonContext =
        mockk(relaxed = true) {
            every { this@mockk.id } returns id
            every { featureManager } returns fm
            every { this@mockk.paths } returns paths
        }

    context("onPrepare") {

        should("reject overridden AddonContextSpec.id") {
            val addon = addon("addon", overrideId = "hacked")

            shouldThrow<IllegalArgumentException> {
                OmsAddons.onPrepare(listOf(addon))
            }
        }

        should("register Forge listeners for features") {
            val listener = Any()

            val feature = mockk<OmsFeature<*>>(relaxed = true) {
                every { createEventListeners() } returns listOf(listener)
            }

            val fm = mockk<FeatureManager>(relaxed = true) {
                every { features() } returns listOf(feature)
            }

            every {
                DefaultAddonContextFactory.create(any())
            } returns context("addon", fm)

            OmsAddons.onPrepare(listOf(addon("addon")))

            verify { FORGE_BUS.register(listener) }
            verify { fm.freeze() }
        }
    }

    context("onServerReady") {

        should("install hook, init root, initialize paths and register configs") {
            val fm = mockk<FeatureManager>(relaxed = true)
            val paths = mockk<AddonPaths>(relaxed = true)

            every {
                DefaultAddonContextFactory.create(any())
            } returns context("addon", fm, paths)

            OmsAddons.onPrepare(listOf(addon("addon")))

            clearMocks(fm, paths, StopManager, OMSRootPath, answers = false)

            val server = mockk<MinecraftServer>(relaxed = true)
            OmsAddons.onServerReady(server)

            verifySequence {
                StopManager.installHook()
                OMSRootPath.init(server)
                OMSRootPath.root
                paths.onInitializeOmsRoot(any())
                fm.onRegisterConfig()
            }
        }
    }

    context("lifecycle forwarding") {

        should("forward StartingEvent") {
            val fm = mockk<FeatureManager>(relaxed = true)
            val ctx = context("addon", fm)

            every {
                DefaultAddonContextFactory.create(any())
            } returns ctx

            OmsAddons.onPrepare(listOf(addon("addon")))

            val event = OMSLifecycle.StartingEvent(mockk(relaxed = true))
            OmsAddons.onOmsStarted(event)

            verify { fm.onStartingEvent(event, ctx) }
        }

        should("forward TickingEvent") {
            val fm = mockk<FeatureManager>(relaxed = true)
            val ctx = context("addon", fm)

            every {
                DefaultAddonContextFactory.create(any())
            } returns ctx

            OmsAddons.onPrepare(listOf(addon("addon")))

            val event = OMSLifecycle.TickingEvent(mockk(relaxed = true), false)
            OmsAddons.onOmsTick(event)

            verify { fm.onTickingEvent(event, ctx) }
        }

        should("forward StoppingEvent") {
            val fm = mockk<FeatureManager>(relaxed = true)
            val ctx = context("addon", fm)

            every {
                DefaultAddonContextFactory.create(any())
            } returns ctx

            OmsAddons.onPrepare(listOf(addon("addon")))

            val event = OMSLifecycle.StoppingEvent(mockk(relaxed = true))
            OmsAddons.onOmsStopping(event)

            verify { fm.onStoppingEvent(event, ctx) }
        }
    }

    context("info & get") {

        should("return addon info") {
            every {
                DefaultAddonContextFactory.create(any())
            } returns context("a")

            OmsAddons.onPrepare(listOf(addon("a")))

            OmsAddons.get("a").shouldBeInstanceOf<AddonInstance>()
            OmsAddons.info().addonsInfo.map { it.id }
                .shouldContainExactly(listOf("a"))
        }
    }

    context("validateAddonId") {

        should("reject duplicate addon id") {
            every {
                DefaultAddonContextFactory.create(any())
            } returns context("dup")

            OmsAddons.onPrepare(listOf(addon("dup")))

            val ex = shouldThrow<IllegalArgumentException> {
                OmsAddons.onPrepare(listOf(addon("dup")))
            }

            ex.message shouldBe "Addon 'dup' already registered"
        }

        should("reject invalid addon id format") {
            val invalidIds = listOf(
                "UpperCase",
                "with space",
                "with.dot",
                "with@symbol",
                "русский",
                "camelCase",
                "slash/id"
            )

            invalidIds.forEach { id ->
                clearRegistry()

                val ex = shouldThrow<IllegalArgumentException> {
                    OmsAddons.onPrepare(listOf(addon(id)))
                }

                ex.message shouldBe
                        "Invalid addon id: $id. It must only contain lowercase letters, numbers, underscores, and hyphens."
            }
        }

        should("accept valid addon id") {
            every {
                DefaultAddonContextFactory.create(any())
            } returns context("valid_id-123")

            OmsAddons.onPrepare(listOf(addon("valid_id-123")))

            OmsAddons.get("valid_id-123")
                .shouldBeInstanceOf<AddonInstance>()
        }
    }

})
