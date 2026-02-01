package io.conboi.oms.infrastructure.file

import io.conboi.oms.api.extension.ensure
import java.nio.file.Path
import net.minecraft.server.MinecraftServer

object OMSRootPath {
    private var cachedRootPath: Path? = null

    fun init(server: MinecraftServer) {
        val serverDirectory = server.serverDirectory.toPath()
        cachedRootPath = serverDirectory.ensure("oms")
    }

    internal val root: Path
        get() = cachedRootPath ?: error("OMSPaths not initialized — wait for OMSLifecycle.StartingEvent to be fired")

}