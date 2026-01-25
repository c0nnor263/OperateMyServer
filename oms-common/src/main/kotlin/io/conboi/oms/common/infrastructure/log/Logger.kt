package io.conboi.oms.common.infrastructure.log

import io.conboi.oms.common.OperateMyServer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

val LOG: Logger = LogManager.getLogger(OperateMyServer.MOD_ID.uppercase())