package io.conboi.oms.common

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

// TODO: Add a Fabric Version
object OperateMyServer {
    const val MOD_ID: String = "oms"

    val LOGGER: Logger = LogManager.getLogger(MOD_ID.uppercase())
}