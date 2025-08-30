package io.conboi.operatemyserver.feature.autorestart

import io.conboi.operatemyserver.common.content.StopManager
import io.conboi.operatemyserver.common.foundation.StopState
import io.conboi.operatemyserver.common.foundation.TimeFormatter
import io.conboi.operatemyserver.common.foundation.TimeHelper
import io.conboi.operatemyserver.common.foundation.feature.FeatureInfo
import io.conboi.operatemyserver.common.foundation.feature.OmsFeature
import io.conboi.operatemyserver.feature.autorestart.infrastructure.config.CAutoRestartFeature
import net.minecraft.network.chat.Component
import net.minecraftforge.event.TickEvent
import java.time.LocalTime
import java.time.ZonedDateTime
import kotlin.time.Duration

// TODO: Check for nextTarget nullability,
//  and if it is null, then pick the closest target.
//  Split tick's method logic into a separate methods
class AutoRestartFeature(featureConfig: CAutoRestartFeature) :
    OmsFeature<CAutoRestartFeature>(featureConfig, FeatureInfo(type = FeatureInfo.Type.AUTO_RESTART, priority = 0)) {

    private var nextTarget: ZonedDateTime? = null
    override fun onServerTick(event: TickEvent.ServerTickEvent) {
        if (nextTarget != null) return
        val server = event.server

        val times: List<LocalTime> = featureConfig.restartTimes.get()
            .mapNotNull(TimeFormatter::parseToLocalTimeOrNull)
            .sortedBy { it.toSecondOfDay() }

        val warningDurations: List<Duration> = featureConfig.warningTimes.get()
            // TODO: Optimize by parsing once and storing the result
            .mapNotNull(TimeFormatter::parseToDurationOrNull)
            .sortedByDescending { it.inWholeSeconds }

        val now = TimeHelper.currentTime

        val target = (nextTarget ?: TimeFormatter.pickClosestTarget(now, times)).also { nextTarget = it }
        val remainingSec = TimeFormatter.secondsBetween(now, target)
        warningDurations.forEach { duration ->
            val seconds = duration.inWholeSeconds
            if (remainingSec == seconds) {
                server.playerList.broadcastSystemMessage(
                    Component.literal("Restart in ${TimeFormatter.formatShort(duration)}!"),
                    false
                )
            }
        }

        if (remainingSec <= 0) {
            StopManager.stop(server, StopState.SCHEDULED)
            nextTarget = null

            // TODO: Set isServerRestarting to true to prevent other features from executing
        }
    }
}