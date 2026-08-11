package io.conboi.oms.watchdogessentials.feature.lowmemory

import io.conboi.oms.api.event.OMSActions
import io.conboi.oms.api.event.OMSLifecycle
import io.conboi.oms.api.foundation.CachedField
import io.conboi.oms.api.foundation.addon.AddonContext
import io.conboi.oms.api.foundation.feature.Priority
import io.conboi.oms.api.infrastructure.config.ConfigProvider
import io.conboi.oms.common.foundation.TimeHelper
import io.conboi.oms.watchdogessentials.common.infrastructure.LOG
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.ByteFormatter
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.CriticalAction
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.MemorySnapshotHistory
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.RuntimeMemoryProvider
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.dump.HeapDumpManager
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.model.MemoryReport
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.model.MemorySnapshot
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.model.RetentionSummary
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.report.MemoryReportManager
import io.conboi.oms.watchdogessentials.feature.lowmemory.infrastructure.config.CLowMemoryFeature
import io.conboi.oms.watchdogessentials.feature.lowmemory.infrastructure.config.cooldown.CCooldowns
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.verify
import java.lang.reflect.Field
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import net.minecraft.server.MinecraftServer
import net.minecraft.server.players.PlayerList
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

class LowMemoryFeatureTest : ShouldSpec({

    lateinit var sut: LowMemoryFeature

    val mockConfig: CLowMemoryFeature = mockk(relaxed = true)
    val mockCooldowns = mockk<CCooldowns>(relaxed = true)
    val mockConfigProvider = mockk<ConfigProvider<CLowMemoryFeature>>()

    val mockServer = mockk<MinecraftServer>(relaxed = true)
    val mockPlayers = mockk<PlayerList>(relaxed = true)

    val mockTickingEvent: OMSLifecycle.TickingEvent = mockk()
    val mockStartingEvent: OMSLifecycle.StartingEvent = mockk(relaxed = true)
    val mockStoppingEvent: OMSLifecycle.StoppingEvent = mockk(relaxed = true)

    val mockAddonContext: AddonContext = mockk(relaxed = true)
    val mockHeapDumpManager = mockk<HeapDumpManager>(relaxed = true)
    val mockMemoryReportManager = mockk<MemoryReportManager>(relaxed = true)

    beforeSpec {
        mockkObject(LOG)
        mockkObject(FORGE_BUS)
        mockkObject(RuntimeMemoryProvider)
        mockkObject(TimeHelper)
    }

    beforeEach {
        every { mockConfigProvider.get() } returns mockConfig
        every { mockConfig.cooldowns } returns mockCooldowns

        every { mockConfig.isEnabled() } returns true
        every { mockConfig.enable() } just Runs
        every { mockConfig.disable() } just Runs
        every { mockConfig.startupCheck.get() } returns true
        every { mockConfig.averagingWindow.get() } returns "5m"
        every { mockConfig.availableThresholdPercent.get() } returns 10.0
        every { mockConfig.criticalAction.get() } returns CriticalAction.WARNING.name
        every { mockConfig.createHeapDumpOnAction.get() } returns false
        every { mockCooldowns.warning.get() } returns "10s"
        every { mockCooldowns.memoryReport.get() } returns "3m"
        every { mockCooldowns.heapDump.get() } returns "5m"

        every { mockTickingEvent.server } returns mockServer
        every { mockServer.playerList } returns mockPlayers
        every { mockServer.tickCount } returns 300
        every { mockPlayers.broadcastSystemMessage(any(), false) } just Runs
        every { FORGE_BUS.post(any()) } returns true
        every { LOG.warn(any<String>()) } just Runs
        every { LOG.error(any<String>(), any<Throwable>()) } just Runs
        every { TimeHelper.currentEpochSeconds } returns 1_000L
        every { RuntimeMemoryProvider.snapshot() } returns lowSnapshot()

        sut = LowMemoryFeature(mockConfigProvider)
        sut.onOmsRegisterConfig()
        sut.injectPrivate("heapDumpManager", mockHeapDumpManager)
        sut.injectPrivate("memoryReportManager", mockMemoryReportManager)
    }

    afterEach {
        clearAllMocks()
    }

    context("info") {
        should("expose id, priority and commands") {
            val info = sut.info()

            info.id shouldBe CLowMemoryFeature.NAME
            info.priority shouldBe Priority.CRITICAL
            info.additionalCommands.map { it::class.simpleName } shouldContainExactly listOf(
                "HeapDumpCommand",
                "SummaryCommand",
                "MemoryReportCommand"
            )
        }
    }

    context("config fields") {
        should("parse and cache averagingWindow") {
            sut.cachedFieldDelegate<Duration>("averagingWindow").get() shouldBe 5.minutes
            sut.cachedFieldDelegate<Duration>("averagingWindow").get() shouldBe 5.minutes
        }

        should("throw when averagingWindow cannot be parsed") {
            every { mockConfig.averagingWindow.get() } returns "bad"

            shouldThrow<IllegalStateException> {
                sut.cachedFieldDelegate<Duration>("averagingWindow").invalidate()
            }.message shouldBe "Cannot parse averagingWindow"
        }

        should("expose simple config values") {
            sut.availableThresholdPercent shouldBe 10.0
            sut.criticalAction shouldBe CriticalAction.WARNING
            sut.createHeapDumpOnAction shouldBe false
        }

        should("throw when criticalAction cannot be parsed") {
            every { mockConfig.criticalAction.get() } returns "bad"

            shouldThrow<IllegalArgumentException> {
                sut.cachedFieldDelegate<CriticalAction>("criticalAction").invalidate()
            }
        }

        should("fall back to default cooldowns when config values are invalid") {
            every { mockCooldowns.warning.get() } returns "bad"
            every { mockCooldowns.memoryReport.get() } returns "bad"
            every { mockCooldowns.heapDump.get() } returns "bad"

            sut.cachedFieldDelegate<Duration>("warningCooldownDuration").get() shouldBe LowMemoryFeature.DEFAULT_WARNING_COOLDOWN
            sut.cachedFieldDelegate<Duration>("memoryReportCooldownDuration").get() shouldBe MemoryReportManager.DEFAULT_MEMORY_REPORT_COOLDOWN
            sut.cachedFieldDelegate<Duration>("heapDumpCooldownDuration").get() shouldBe HeapDumpManager.DEFAULT_HEAP_DUMP_COOLDOWN
        }

        should("recreate memorySnapshotHistory when averagingWindow changes") {
            val first = sut.cachedFieldDelegate<MemorySnapshotHistory>("memorySnapshotHistory").get()
            val second = sut.cachedFieldDelegate<MemorySnapshotHistory>("memorySnapshotHistory").get()

            (first === second) shouldBe true

            every { mockConfig.averagingWindow.get() } returns "10m"
            sut.cachedFieldDelegate<Duration>("averagingWindow").invalidate()

            val third = sut.cachedFieldDelegate<MemorySnapshotHistory>("memorySnapshotHistory").get()
            (first === third) shouldBe false
            third.retentionWindow shouldBe 10.minutes
        }

        should("reset dependent cooldowns when their fields change") {
            every { mockCooldowns.memoryReport.get() } returns "4m"
            every { mockCooldowns.heapDump.get() } returns "6m"

            sut.cachedFieldDelegate<Duration>("memoryReportCooldownDuration").invalidate()
            sut.cachedFieldDelegate<Duration>("heapDumpCooldownDuration").invalidate()

            verify(exactly = 1) { mockMemoryReportManager.resetCooldown() }
            verify(exactly = 1) { mockHeapDumpManager.resetCooldown() }
        }
    }

    context("request helpers") {
        should("return null summary when no snapshots exist") {
            sut.requestSummary() shouldBe null
        }

        should("return the latest snapshot summary") {
            val first = snapshot(createdAt = 700L, allocatedBytes = 60L, freeAllocatedBytes = 20L)
            val second = snapshot(createdAt = 800L, allocatedBytes = 80L, freeAllocatedBytes = 30L)

            sut.memorySnapshotHistory.add(first)
            sut.memorySnapshotHistory.add(second)

            sut.requestSummary() shouldBe second
        }

        should("summarize snapshots over a requested window") {
            sut.memorySnapshotHistory.add(snapshot(createdAt = 699L, allocatedBytes = 60L, freeAllocatedBytes = 20L))
            sut.memorySnapshotHistory.add(snapshot(createdAt = 700L, allocatedBytes = 70L, freeAllocatedBytes = 20L))
            sut.memorySnapshotHistory.add(snapshot(createdAt = 800L, allocatedBytes = 80L, freeAllocatedBytes = 30L))

            val summary = sut.requestSummaryOver(5.minutes)

            summary shouldBe RetentionSummary(
                snapshotsCount = 2,
                retentionWindow = 5.minutes,
                averageUsedBytes = 50L,
                averageAvailableBytes = 50L,
                averageUsedPercent = 50.0,
                averageAvailablePercent = 50.0,
                minAvailableBytes = 50L,
                maxUsedBytes = 50L
            )
        }

        should("request heap dump on the next tick") {
            sut.requestHeapDump()
            every { mockServer.tickCount } returns 1

            sut.onOmsTick(mockTickingEvent, mockAddonContext)

            verify(exactly = 1) { mockHeapDumpManager.requestHeapDumpAsync(mockAddonContext) }
            verify(exactly = 0) { mockMemoryReportManager.requestReportAsync(any(), any()) }
        }

        should("request memory report on the next tick") {
            sut.requestReport()
            every { mockServer.tickCount } returns 1

            sut.onOmsTick(mockTickingEvent, mockAddonContext)

            verify(exactly = 1) { mockMemoryReportManager.requestReportAsync(mockAddonContext, any()) }
            verify(exactly = 0) { mockHeapDumpManager.requestHeapDumpAsync(any()) }
        }
    }

    context("onOmsStarted") {
        should("warn when startup memory is below recommendation") {
            every {
                RuntimeMemoryProvider.snapshot()
            } returns snapshot(
                createdAt = 1_000L,
                maxBytes = 1_024L
            )

            sut.onOmsStarted(mockStartingEvent, mockAddonContext)

            verify {
                LOG.warn(
                    "OMS detected that the server is running with less than ${ByteFormatter.format(
                        LowMemoryFeature.RECOMMENDED_SERVER_MAX_MEMORY
                    )} max JVM memory.\n" +
                        "This may be enough for small/test servers, but is not recommended for production Minecraft servers"
                )
            }
        }

        should("skip warning when startup check is disabled") {
            every { mockConfig.startupCheck.get() } returns false

            sut.onOmsStarted(mockStartingEvent, mockAddonContext)

            verify(exactly = 0) { RuntimeMemoryProvider.snapshot() }
        }

        should("skip warning when startup memory is sufficient") {
            every {
                RuntimeMemoryProvider.snapshot()
            } returns snapshot(
                createdAt = 1_000L,
                maxBytes = LowMemoryFeature.RECOMMENDED_SERVER_MAX_MEMORY
            )

            sut.onOmsStarted(mockStartingEvent, mockAddonContext)

            verify(exactly = 0) { LOG.warn(any<String>()) }
        }
    }

    context("onOmsTick") {
        should("not run low-memory logic before the timer fires") {
            every { mockServer.tickCount } returns 299

            sut.onOmsTick(mockTickingEvent, mockAddonContext)

            verify(exactly = 0) { RuntimeMemoryProvider.snapshot() }
            verify(exactly = 0) { mockMemoryReportManager.createReport(any(), any(), any()) }
            verify(exactly = 0) { mockHeapDumpManager.createHeapDump(any(), any()) }
            verify(exactly = 0) { FORGE_BUS.post(any()) }
            verify(exactly = 0) { mockPlayers.broadcastSystemMessage(any(), any()) }
        }

        should("create a report, heap dump and stop event when action is restart") {
            every { mockConfig.criticalAction.get() } returns CriticalAction.RESTART.name
            every { mockConfig.createHeapDumpOnAction.get() } returns true

            val stopSlot = slot<OMSActions.StopRequestedEvent>()
            val reportSlot = slot<MemoryReport>()
            every { FORGE_BUS.post(capture(stopSlot)) } returns true
            every {
                mockMemoryReportManager.createReport(
                    mockAddonContext,
                    capture(reportSlot),
                    3.minutes
                )
            } just Runs

            sut.onOmsTick(mockTickingEvent, mockAddonContext)

            verify(exactly = 1) {
                mockMemoryReportManager.createReport(mockAddonContext, any(), 3.minutes)
            }
            verify(exactly = 1) {
                mockHeapDumpManager.createHeapDump(mockAddonContext, 5.minutes)
            }
            verify(exactly = 1) { FORGE_BUS.post(any()) }
            verify(exactly = 0) { mockPlayers.broadcastSystemMessage(any(), any()) }

            reportSlot.captured.action shouldBe CriticalAction.RESTART
            reportSlot.captured.currentSnapshot shouldBe lowSnapshot()
            reportSlot.captured.historySummary shouldBe RetentionSummary(
                snapshotsCount = 1,
                retentionWindow = 5.minutes,
                averageUsedBytes = 100L,
                averageAvailableBytes = 0L,
                averageUsedPercent = 100.0,
                averageAvailablePercent = 0.0,
                minAvailableBytes = 0L,
                maxUsedBytes = 100L
            )
            reportSlot.captured.memoryCountTime shouldBe 5.minutes
            reportSlot.captured.thresholdPercent shouldBe 10.0
            stopSlot.captured.reason.name shouldBe "low_memory_restart"
        }

        should("post a shutdown stop event when action is shutdown") {
            every { mockConfig.criticalAction.get() } returns CriticalAction.SHUTDOWN.name

            val stopSlot = slot<OMSActions.StopRequestedEvent>()
            every { FORGE_BUS.post(capture(stopSlot)) } returns true

            sut.onOmsTick(mockTickingEvent, mockAddonContext)

            verify(exactly = 1) { FORGE_BUS.post(any()) }
            stopSlot.captured.reason.name shouldBe "low_memory_shutdown"
        }

        should("broadcast warning once until cooldown expires and reset on config update") {
            every { mockConfig.criticalAction.get() } returns CriticalAction.WARNING.name
            every { mockConfig.createHeapDumpOnAction.get() } returns false

            sut.onOmsTick(mockTickingEvent, mockAddonContext)
            sut.onOmsTick(mockTickingEvent, mockAddonContext)

            every { mockCooldowns.warning.get() } returns "30s"
            sut.cachedFieldDelegate<Duration>("warningCooldownDuration").invalidate()
            sut.onOmsTick(mockTickingEvent, mockAddonContext)

            verify(exactly = 2) { mockPlayers.broadcastSystemMessage(any(), false) }
            verify(exactly = 3) { mockMemoryReportManager.createReport(mockAddonContext, any(), 3.minutes) }
            verify(exactly = 0) { mockHeapDumpManager.createHeapDump(any(), any()) }
            verify(exactly = 0) { FORGE_BUS.post(any()) }
        }

        should("stop processing low memory after a stop has been requested") {
            every { mockConfig.criticalAction.get() } returns CriticalAction.RESTART.name
            every { mockConfig.createHeapDumpOnAction.get() } returns true

            every { FORGE_BUS.post(any()) } returns true

            sut.onOmsTick(mockTickingEvent, mockAddonContext)
            sut.onOmsTick(mockTickingEvent, mockAddonContext)

            verify(exactly = 1) { mockMemoryReportManager.createReport(mockAddonContext, any(), 3.minutes) }
            verify(exactly = 1) { mockHeapDumpManager.createHeapDump(mockAddonContext, 5.minutes) }
            verify(exactly = 1) { FORGE_BUS.post(any()) }
            verify(exactly = 0) { mockPlayers.broadcastSystemMessage(any(), any()) }
        }

        should("stop processing low memory if averageAvailableMemoryPercent is above threshold") {
            every { mockConfig.criticalAction.get() } returns CriticalAction.WARNING.name
            every { mockConfig.createHeapDumpOnAction.get() } returns false

            every { RuntimeMemoryProvider.snapshot() } returns snapshot(
                createdAt = 1_000L,
                maxBytes = 100L,
                allocatedBytes = 100L,
                freeAllocatedBytes = 20L
            )

            sut.onOmsTick(mockTickingEvent, mockAddonContext)

            verify(exactly = 0) { mockPlayers.broadcastSystemMessage(any(), any()) }
            verify(exactly = 0) { mockMemoryReportManager.createReport(any(), any(), any()) }
            verify(exactly = 0) { mockHeapDumpManager.createHeapDump(any(), any()) }
        }
    }

    context("onOmsStopping") {
        should("clear background managers") {
            sut.onOmsStopping(mockStoppingEvent, mockAddonContext)

            verify(exactly = 1) { mockHeapDumpManager.clear() }
            verify(exactly = 1) { mockMemoryReportManager.clear() }
        }
    }
})

private fun lowSnapshot(): MemorySnapshot {
    return snapshot(
        createdAt = 1_000L,
        maxBytes = 100L,
        allocatedBytes = 100L,
        freeAllocatedBytes = 0L
    )
}

private fun snapshot(
    createdAt: Long,
    maxBytes: Long = 100L,
    allocatedBytes: Long = maxBytes,
    freeAllocatedBytes: Long = 0L
): MemorySnapshot {
    return MemorySnapshot(
        createdAt = createdAt,
        maxBytes = maxBytes,
        allocatedBytes = allocatedBytes,
        freeAllocatedBytes = freeAllocatedBytes
    )
}

private inline fun <reified T> LowMemoryFeature.cachedFieldDelegate(fieldName: String): CachedField<*, T> {
    val field = javaClass.getDeclaredField("$fieldName\$delegate")
    field.isAccessible = true
    @Suppress("UNCHECKED_CAST")
    return field.get(this) as CachedField<*, T>
}

private fun LowMemoryFeature.injectPrivate(fieldName: String, value: Any) {
    val field: Field = javaClass.getDeclaredField(fieldName)
    field.isAccessible = true
    field.set(this, value)
}
