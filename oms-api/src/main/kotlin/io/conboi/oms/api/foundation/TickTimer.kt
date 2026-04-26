package io.conboi.oms.api.foundation

/**
 * A lightweight tick-based timer.
 *
 * [TickTimer] fires at a fixed interval measured in server ticks and is
 * typically used to throttle periodic operations within the OMS runtime
 * (for example, feature ticking or diagnostics updates).
 *
 * The default interval is 20 ticks (approximately 1 second).
 *
 * @param intervalTicks the number of server ticks between timer firings
 */
class TickTimer(
    private val intervalTicks: Int = 20
) {

    /**
     * Determines whether the timer should fire at the given server tick.
     *
     * @param serverTickCount the current server tick count
     * @return `true` if the timer fires on this tick, `false` otherwise
     */
    fun shouldFire(serverTickCount: Int): Boolean {
        return serverTickCount % intervalTicks == 0
    }
}
