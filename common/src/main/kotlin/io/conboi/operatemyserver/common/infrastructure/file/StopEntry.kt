package io.conboi.operatemyserver.common.infrastructure.file

import kotlinx.serialization.Serializable

@Serializable
data class StopEntry(
    val reason: String,
    val time: String
)