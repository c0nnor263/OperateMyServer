package io.conboi.oms.watchdogessentials.feature.lowtps.foundation

import io.conboi.oms.common.foundation.TimeHelper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkObject
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class TpsSnapshotHistoryTest : ShouldSpec({

    beforeSpec {
        mockkObject(TimeHelper)
    }

    beforeEach {
        every { TimeHelper.currentEpochSeconds } returns 1_000L
    }

    afterEach {
        clearAllMocks()
    }

    context("averageTps") {
        should("return default TPS when no snapshots exist") {
            val history = TpsSnapshotHistory(5.minutes, 1.seconds)

            history.averageTps() shouldBe TpsSnapshotHistory.DEFAULT_TPS
        }

        should("average only snapshots inside the retention window") {
            val history = TpsSnapshotHistory(5.minutes, 1.seconds)
            history.add(snapshot(699, 10.0))
            history.add(snapshot(700, 20.0))
            history.add(snapshot(800, 30.0))

            history.averageTps() shouldBe 25.0
        }
    }

    context("summary") {
        should("summarize the retained history") {
            val history = TpsSnapshotHistory(5.minutes, 1.seconds)
            history.add(snapshot(699, 10.0))
            history.add(snapshot(700, 20.0))
            history.add(snapshot(800, 30.0))

            val summary = history.summary()

            summary.snapshotsCount shouldBe 3
        }
    }

    context("summaryOver") {
        should("summarize only the requested window") {
            val history = TpsSnapshotHistory(5.minutes, 1.seconds)
            history.add(snapshot(699, 10.0))
            history.add(snapshot(700, 20.0))
            history.add(snapshot(800, 30.0))

            val summary = history.summaryOver(5.minutes)

            summary.snapshotsCount shouldBe 2
        }

        should("reject non-positive windows") {
            val history = TpsSnapshotHistory(5.minutes, 1.seconds)

            shouldThrow<IllegalArgumentException> {
                history.summaryOver(0.minutes)
            }.message shouldBe "window must be positive"
        }
    }
})

private fun snapshot(createdAt: Long, value: Double): TpsSnapshot {
    return TpsSnapshot(
        createdAt = createdAt,
        value = value
    )
}
