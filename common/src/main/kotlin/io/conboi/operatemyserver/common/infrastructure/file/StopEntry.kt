package io.conboi.operatemyserver.common.infrastructure.file

import io.conboi.operatemyserver.common.foundation.StopState
import kotlinx.serialization.Serializable

@Serializable
data class StopEntry(
    val state: StopState,
    val time: String
)