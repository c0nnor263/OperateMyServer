package io.conboi.oms.event.listener

import io.conboi.oms.api.event.OMSActions
import io.conboi.oms.common.OperateMyServer
import io.conboi.oms.foundation.StopManager
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber(modid = OperateMyServer.MOD_ID)
internal object OMSActionsListener {
    @SubscribeEvent
    fun onStopRequestedEvent(event: OMSActions.StopRequestedEvent) {
        StopManager.scheduleStop(event)
    }
}