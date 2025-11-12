package io.conboi.oms.api.event

import io.conboi.oms.api.infrastructure.config.FeatureConfig

interface OMSFeatureLifecycle {
    fun onOmsTick(event: OMSLifecycle.TickingEvent)
    fun onOmsStarted(event: OMSLifecycle.StartingEvent) {}
    fun onOmsStopping(event: OMSLifecycle.StoppingEvent) {}
    fun onOmsRegisterConfig(config: FeatureConfig)

    fun onEnabled()
    fun onDisabled()
}