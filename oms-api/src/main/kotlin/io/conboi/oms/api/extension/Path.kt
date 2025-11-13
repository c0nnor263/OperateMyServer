package io.conboi.oms.api.extension

import java.nio.file.Files
import java.nio.file.Path

fun Path.ensure(name: String): Path {
    val path = this.resolve(name)
    Files.createDirectories(path)
    return path
}