package io.conboi.oms.watchdogessentials.feature.lowmemory.foundation

import io.conboi.oms.common.foundation.TimeHelper
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.model.MemorySnapshot
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkObject
import kotlin.time.Duration.Companion.minutes

class MemorySnapshotHistoryTest : ShouldSpec({

    beforeSpec {
        mockkObject(TimeHelper)
    }

    beforeEach {
        every { TimeHelper.currentEpochSeconds } returns 1_000L
    }

    afterEach {
        clearAllMocks()
    }

    context("averageAvailablePercent") {
        should("return default when no snapshots exist") {
            val history = MemorySnapshotHistory(5.minutes, 1.minutes)

            history.averageAvailablePercent() shouldBe MemorySnapshotHistory.DEFAULT_MEMORY_AVAILABLE_PERCENT
        }

        should("average only snapshots inside the retention window") {
            val history = MemorySnapshotHistory(5.minutes, 1.minutes)
            history.add(snapshot(699, 40, 10))
            history.add(snapshot(700, 80, 30))
            history.add(snapshot(800, 90, 40))

            history.averageAvailablePercent() shouldBe 50.0
        }
    }

    context("summary") {
        should("summarize the full retained history") {
            val history = MemorySnapshotHistory(5.minutes, 1.minutes)
            history.add(snapshot(699, 40, 10))
            history.add(snapshot(700, 80, 30))
            history.add(snapshot(800, 90, 40))

            val summary = history.summary()

            summary.snapshotsCount shouldBe 3
            summary.retentionWindow shouldBe 5.minutes
            summary.averageUsedBytes shouldBe 43L
            summary.averageAvailableBytes shouldBe 56L
            summary.averageUsedPercent shouldBe 43.333333333333336
            summary.averageAvailablePercent shouldBe 56.666666666666664
            summary.minAvailableBytes shouldBe 50L
            summary.maxUsedBytes shouldBe 50L
        }
    }

    context("summaryOver") {
        should("summarize only snapshots inside the requested window") {
            val history = MemorySnapshotHistory(5.minutes, 1.minutes)
            history.add(snapshot(699, 40, 10))
            history.add(snapshot(700, 80, 30))
            history.add(snapshot(800, 90, 40))

            val summary = history.summaryOver(5.minutes)

            summary.snapshotsCount shouldBe 2
            summary.retentionWindow shouldBe 5.minutes
            summary.averageUsedBytes shouldBe 50L
            summary.averageAvailableBytes shouldBe 50L
            summary.averageUsedPercent shouldBe 50.0
            summary.averageAvailablePercent shouldBe 50.0
            summary.minAvailableBytes shouldBe 50L
            summary.maxUsedBytes shouldBe 50L
        }

        should("reject non-positive windows") {
            val history = MemorySnapshotHistory(5.minutes, 1.minutes)

            shouldThrow<IllegalArgumentException> {
                history.summaryOver(0.minutes)
            }.message shouldBe "window must be positive"
        }
    }
})

private fun snapshot(createdAt: Long, allocatedBytes: Long, freeAllocatedBytes: Long): MemorySnapshot {
    return MemorySnapshot(
        createdAt = createdAt,
        maxBytes = 100L,
        allocatedBytes = allocatedBytes,
        freeAllocatedBytes = freeAllocatedBytes
    )
}
