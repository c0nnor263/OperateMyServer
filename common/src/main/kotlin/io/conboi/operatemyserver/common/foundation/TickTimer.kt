package io.conboi.operatemyserver.common.foundation

class TickTimer(private val intervalTicks: Int = 20) {
    fun shouldFire(serverTickCount: Int): Boolean {
        return serverTickCount % intervalTicks == 0
    }
}