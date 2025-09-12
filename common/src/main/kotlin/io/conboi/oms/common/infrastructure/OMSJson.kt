package io.conboi.oms.common.infrastructure

import kotlinx.serialization.json.Json

val OMSJson = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    encodeDefaults = true
}