package io.conboi.oms.common.infrastructure.file

import net.minecraftforge.event.server.ServerStartedEvent
import java.nio.file.Path

object OMSPaths {
    private var cachedRootPath: Path? = null

    fun root(): Path {
        return cachedRootPath ?: throw IllegalStateException("Server directory is not available")
    }

    fun stopCause(): Path = root().resolve("stop_cause.json")

    fun init(event: ServerStartedEvent) {
        val rootPath = event.server.serverDirectory.toPath().resolve("oms")
        cachedRootPath = rootPath
        if (!rootPath.toFile().exists()) {
            rootPath.toFile().mkdirs()
        }
    }
}