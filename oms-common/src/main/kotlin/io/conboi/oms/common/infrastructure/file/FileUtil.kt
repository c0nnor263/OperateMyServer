package io.conboi.oms.common.infrastructure.file

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.writeText

object FileUtil {
    fun ensureDir(path: Path) {
        if (!path.exists()) path.createDirectories()
    }

    fun writeSafe(path: Path, content: String) {
        ensureDir(path.parent)
        val tmp = path.resolveSibling(path.fileName.toString() + ".tmp")
        tmp.writeText(content)
        Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING)
    }
}