package io.conboi.oms.api.foundation.logging

import org.apache.logging.log4j.Logger

class OMSPersistentFileLogger internal constructor(
    internal val logger: Logger
) : PersistentLogger {

    fun info(msg: String, vararg args: Any?) = logger.info(msg, *args)
    fun warn(msg: String, vararg args: Any?) = logger.warn(msg, *args)
    fun error(msg: String, vararg args: Any?) = logger.error(msg, *args)
    fun debug(msg: String, vararg args: Any?) = logger.debug(msg, *args)

    fun raw(): Logger = logger
}
