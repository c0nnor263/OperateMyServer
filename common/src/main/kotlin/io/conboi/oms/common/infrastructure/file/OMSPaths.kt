package io.conboi.oms.common.infrastructure.file

import java.nio.file.Path
import net.minecraft.server.MinecraftServer

object OMSPaths {
    private var cachedRootPath: Path? = null

    fun root(): Path {
        return cachedRootPath ?: throw IllegalStateException("Server directory is not available")
    }

    fun stopCause(): Path = root().resolve("stop_cause.json")

    fun init(server: MinecraftServer) {
        val rootPath = server.serverDirectory.toPath().resolve("oms")
        cachedRootPath = rootPath
        if (!rootPath.toFile().exists()) {
            rootPath.toFile().mkdirs()
        }
    }
}