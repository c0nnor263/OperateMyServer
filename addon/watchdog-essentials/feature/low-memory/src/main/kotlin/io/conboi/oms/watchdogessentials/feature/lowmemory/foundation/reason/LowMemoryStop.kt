package io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.reason

import io.conboi.oms.watchdogessentials.common.foundation.reason.WEStopReason
import io.conboi.oms.watchdogessentials.feature.lowmemory.foundation.CriticalAction

sealed class LowMemoryStop(
    override val name: String,
    private val args: Arguments
) : WEStopReason {

    data class Arguments(
        val averageAvailableMemoryPercent: String,
        val memoryCountTime: String,
        val memoryAvailableThresholdPercent: String
    ) {
        fun toArray(): Array<String?> = arrayOf(
            averageAvailableMemoryPercent,
            memoryCountTime,
            memoryAvailableThresholdPercent
        )
    }

    override val arguments: Array<out String?> = args.toArray()

    class Restart(args: Arguments) : LowMemoryStop(
        name = "low_memory_restart",
        args = args
    )

    class Shutdown(args: Arguments) : LowMemoryStop(
        name = "low_memory_shutdown",
        args = args
    ) {
        override val shouldRestart: Boolean = false
    }

    companion object {
        fun fromCriticalAction(
            action: CriticalAction,
            args: Arguments
        ): WEStopReason = when (action) {
            CriticalAction.RESTART -> Restart(args = args)

            CriticalAction.SHUTDOWN -> Shutdown(args = args)

            CriticalAction.WARNING -> error(
                "CriticalAction.WARNING should not be converted to a stop reason"
            )
        }
    }
}