package io.conboi.operatemyserver.common.foundation

import kotlinx.serialization.Serializable

@Serializable
enum class StopState {
    SCHEDULED,
    MANUAL,
    CRASH,
    STOP,
    LOW_TPS,
    UNKNOWN;
}