package io.conboi.oms.core.infrastructure

import kotlinx.serialization.json.Json

val OMSJson = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    encodeDefaults = true
}