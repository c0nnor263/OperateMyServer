package io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.reason

import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.CriticalAction
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class LowMemoryStopTest : ShouldSpec({

    val args = LowMemoryStop.Arguments(
        averageAvailableMemoryPercent = "12.3",
        memoryCountTime = "5m",
        memoryAvailableThresholdPercent = "10.0"
    )

    context("Arguments") {
        should("convert to array in declared order") {
            args.toArray().toList() shouldBe listOf("12.3", "5m", "10.0")
        }
    }

    context("fromCriticalAction") {
        should("create restart reason for restart action") {
            val reason = LowMemoryStop.fromCriticalAction(CriticalAction.RESTART, args)

            reason.name shouldBe "low_memory_restart"
            reason.arguments.toList() shouldBe listOf("12.3", "5m", "10.0")
            reason.shouldRestart shouldBe true
        }

        should("create shutdown reason for shutdown action") {
            val reason = LowMemoryStop.fromCriticalAction(CriticalAction.SHUTDOWN, args)

            reason.name shouldBe "low_memory_shutdown"
            reason.arguments.toList() shouldBe listOf("12.3", "5m", "10.0")
            reason.shouldRestart shouldBe false
        }

        should("reject warning action") {
            shouldThrow<IllegalStateException> {
                LowMemoryStop.fromCriticalAction(CriticalAction.WARNING, args)
            }.message shouldBe "CriticalAction.WARNING should not be converted to a stop reason"
        }
    }
})
