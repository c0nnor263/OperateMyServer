package io.conboi.oms.api.extension

import java.nio.file.Files
import java.nio.file.Path

/**
 * Ensures that a directory with the given [name] exists under this [Path].
 *
 * If the directory does not exist, it is created along with any
 * missing parent directories.
 *
 * If the directory already exists, this operation has no effect.
 *
 * @param name the name of the directory to ensure
 * @return the [Path] to the ensured directory
 *
 * @throws java.io.IOException if the directory cannot be created
 *         due to an I/O error or insufficient permissions
 */
fun Path.ensure(name: String): Path {
    val path = this.resolve(name)
    Files.createDirectories(path)
    return path
}