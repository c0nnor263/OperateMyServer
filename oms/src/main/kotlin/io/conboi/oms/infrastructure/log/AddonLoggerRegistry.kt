package io.conboi.oms.infrastructure.log

import io.conboi.oms.api.infrastructure.log.AddonLogger
import io.conboi.oms.infrastructure.log.persistent.DefaultPersistentLogger
import io.conboi.oms.infrastructure.log.persistent.PersistentLoggerFactory
import io.conboi.oms.infrastructure.log.runtime.DefaultRuntimeLogger
import java.nio.file.Path
import org.apache.logging.log4j.LogManager

internal object AddonLoggerRegistry {

    private val cache = mutableMapOf<String, AddonLogger>()

    fun runtime(name: String): AddonLogger =
        cache.getOrPut(name) {
            val raw = LogManager.getLogger(name)
            DefaultRuntimeLogger(raw = raw)
        }

    fun persistent(parentName: String, dirProvider: () -> Path): AddonLogger {
        val fullName = "$parentName.persist"
        return cache.getOrPut(fullName) {
            val raw = PersistentLoggerFactory.get(fullName, dirProvider.invoke())
            DefaultPersistentLogger(raw = raw)
        }
    }
}