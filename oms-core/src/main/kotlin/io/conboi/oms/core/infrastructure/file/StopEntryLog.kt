package io.conboi.oms.core.infrastructure.file

import kotlinx.serialization.Serializable

@Serializable
data class StopEntryLog(
    val reason: String,
    val message: String,
    val time: String
)