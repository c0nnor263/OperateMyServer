package io.conboi.oms.infrastructure.log.runtime

import io.conboi.oms.infrastructure.log.AddonLoggerImpl
import org.apache.logging.log4j.Logger

class DefaultRuntimeLogger(
    override val raw: Logger,
) : AddonLoggerImpl(raw)