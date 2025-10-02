package io.conboi.oms.api.foundation

/**
 * A simple timer that fires at a specified interval of server ticks.
 * Default interval is 20 ticks (1 second)
 */
class TickTimer(private val intervalTicks: Int = 20) {
    fun shouldFire(serverTickCount: Int): Boolean {
        return serverTickCount % intervalTicks == 0
    }
}