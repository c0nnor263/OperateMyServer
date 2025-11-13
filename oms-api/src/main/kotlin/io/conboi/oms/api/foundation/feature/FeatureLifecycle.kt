package io.conboi.oms.api.foundation.feature

import io.conboi.oms.api.event.OMSLifecycle

interface FeatureLifecycle {
    fun onOmsTick(event: OMSLifecycle.TickingEvent)
    fun onOmsStarted(event: OMSLifecycle.StartingEvent) {}
    fun onOmsStopping(event: OMSLifecycle.StoppingEvent) {}
    fun onOmsRegisterConfig()

    fun onEnabled()
    fun onDisabled()
}