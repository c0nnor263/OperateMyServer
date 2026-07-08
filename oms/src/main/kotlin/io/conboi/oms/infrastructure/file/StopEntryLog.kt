package io.conboi.oms.infrastructure.file

import kotlinx.serialization.Serializable

@Serializable
data class StopEntryLog(
    val reason: String,
    val message: String,
    val shouldRestart: Boolean,
    val time: String
)