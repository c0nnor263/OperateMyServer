package io.conboi.operatemyserver.common.infrastructure.file

import io.conboi.operatemyserver.common.foundation.StopState
import io.conboi.operatemyserver.common.foundation.TimeHelper
import io.conboi.operatemyserver.common.infrastructure.OMSJson

class StopLog {
    fun write(state: StopState) {
        val entry = StopEntry(state, TimeHelper.currentTime.toString())
        val content = OMSJson.encodeToString(StopEntry.serializer(), entry)
        FileUtil.writeSafe(OMSPaths.stopCause(), content)
    }
}