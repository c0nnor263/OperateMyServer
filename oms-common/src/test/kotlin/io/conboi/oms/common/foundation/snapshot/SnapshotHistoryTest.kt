package io.conboi.oms.common.foundation.snapshot

import io.conboi.oms.common.foundation.TimeHelper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkObject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class SnapshotHistoryTest : ShouldSpec({

    beforeSpec {
        mockkObject(TimeHelper)
    }

    beforeEach {
        every { TimeHelper.currentEpochSeconds } returns 1_000L
    }

    afterEach {
        clearAllMocks()
    }

    context("constructor") {
        should("reject non-positive retention window") {
            shouldThrow<IllegalArgumentException> {
                TestHistory(retentionWindow = 0.minutes, checkInterval = 1.minutes)
            }.message shouldBe "countTime must be positive"
        }

        should("reject non-positive check interval") {
            shouldThrow<IllegalArgumentException> {
                TestHistory(retentionWindow = 1.minutes, checkInterval = 0.minutes)
            }.message shouldBe "checkInterval must be positive"
        }

        should("reject zero capacity") {
            shouldThrow<IllegalArgumentException> {
                TestHistory(retentionWindow = 1.minutes, checkInterval = 1.minutes, capacity = 0)
            }.message shouldBe "maxSize must be greater than 0"
        }
    }

    context("add and latest") {
        should("keep only the newest snapshots up to capacity") {
            val history = TestHistory(retentionWindow = 3.minutes, checkInterval = 1.minutes)

            history.add(TestSnapshot(900, 1.0))
            history.add(TestSnapshot(950, 2.0))
            history.add(TestSnapshot(980, 3.0))
            history.add(TestSnapshot(990, 4.0))

            history.latest() shouldBe TestSnapshot(990, 4.0)
            history.summary().snapshotsCount shouldBe 3
            history.summary().average shouldBe 3.0
        }

        should("return null when empty") {
            val history = TestHistory(retentionWindow = 3.minutes, checkInterval = 1.minutes)

            history.latest() shouldBe null
        }
    }

    context("averageOf") {
        should("return default value for empty list") {
            val history = TestHistory(retentionWindow = 3.minutes, checkInterval = 1.minutes)

            history.averageValue(emptyList()) shouldBe 0.0
        }

        should("calculate average for provided snapshots") {
            val history = TestHistory(retentionWindow = 3.minutes, checkInterval = 1.minutes)
            val snapshots = listOf(
                TestSnapshot(900, 2.0),
                TestSnapshot(950, 4.0),
                TestSnapshot(980, 6.0)
            )

            history.averageValue(snapshots) shouldBe 4.0
        }
    }

    context("filteredHistoryOver") {
        should("include only snapshots inside the retention window") {
            val history = TestHistory(retentionWindow = 5.minutes, checkInterval = 1.minutes)
            history.add(TestSnapshot(699, 1.0))
            history.add(TestSnapshot(700, 2.0))
            history.add(TestSnapshot(701, 3.0))
            history.add(TestSnapshot(750, 4.0))

            history.filtered(5.minutes) shouldBe listOf(
                TestSnapshot(700, 2.0),
                TestSnapshot(701, 3.0),
                TestSnapshot(750, 4.0)
            )
        }

        should("reject non-positive windows") {
            val history = TestHistory(retentionWindow = 5.minutes, checkInterval = 1.minutes)

            shouldThrow<IllegalArgumentException> {
                history.filtered(0.minutes)
            }.message shouldBe "window must be positive"
        }
    }
})

private data class TestSnapshot(
    override val createdAt: Long,
    val value: Double
) : Snapshot

private data class TestSummary(
    override val snapshotsCount: Int,
    val average: Double
) : SnapshotHistory.Summary

private class TestHistory(
    retentionWindow: Duration,
    checkInterval: Duration,
    capacity: Int = 3
) : SnapshotHistory<TestSnapshot, TestSummary>(retentionWindow, checkInterval, capacity) {

    override fun summary(): TestSummary {
        return TestSummary(
            snapshotsCount = history.size,
            average = averageOf { it.value }
        )
    }

    override fun summaryOver(window: Duration): TestSummary {
        val snapshots = filteredHistoryOver(window)
        return TestSummary(
            snapshotsCount = snapshots.size,
            average = averageOf(snapshots) { it.value }
        )
    }

    fun averageValue(snapshotList: List<TestSnapshot>): Double {
        return averageOf(snapshotList) { it.value }
    }

    fun filtered(window: Duration): List<TestSnapshot> {
        return filteredHistoryOver(window)
    }
}
