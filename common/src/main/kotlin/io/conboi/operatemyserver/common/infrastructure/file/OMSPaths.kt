package io.conboi.operatemyserver.common.infrastructure.file

import net.minecraftforge.server.ServerLifecycleHooks
import java.nio.file.Path

object OMSPaths {
    private var cachedRootPath: Path? = null

    fun root(): Path {
        cachedRootPath ?: ServerLifecycleHooks.getCurrentServer()?.serverDirectory?.toPath()?.resolve("oms")?.also {
            cachedRootPath = it
        }
        return cachedRootPath ?: throw IllegalStateException("Server directory is not available")
    }

    fun stopCause(): Path = root().resolve("stop_cause.json")

    fun init() {
        val root = root()
        if (!root.toFile().exists()) {
            root.toFile().mkdirs()
        }
    }
}