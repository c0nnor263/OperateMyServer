package io.conboi.oms.infrastructure.log.persistent

import io.conboi.oms.infrastructure.log.AddonLoggerImpl
import org.apache.logging.log4j.Logger

class DefaultPersistentLogger(
    override val raw: Logger,
) : AddonLoggerImpl(raw)