package io.conboi.oms.infrastructure.log

import io.conboi.oms.api.infrastructure.log.AddonLogger
import org.apache.logging.log4j.Logger

abstract class AddonLoggerImpl(open val raw: Logger) : AddonLogger {
    override fun info(msg: String, vararg args: Any?) = raw.info(msg, *args)
    override fun warn(msg: String, vararg args: Any?) = raw.warn(msg, *args)
    override fun error(msg: String, vararg args: Any?) = raw.error(msg, *args)
    override fun debug(msg: String, vararg args: Any?) = raw.debug(msg, *args)
}
