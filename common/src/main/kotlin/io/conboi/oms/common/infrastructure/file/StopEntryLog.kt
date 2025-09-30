package io.conboi.oms.common.infrastructure.file

import kotlinx.serialization.Serializable

@Serializable
data class StopEntryLog(
    val reason: String,
    val time: String
)