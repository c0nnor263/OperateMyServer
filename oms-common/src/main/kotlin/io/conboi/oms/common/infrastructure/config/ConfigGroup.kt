package io.conboi.oms.common.infrastructure.config

import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.common.ForgeConfigSpec.Builder

/**
 * Marker for config subgroups
 */
class ConfigGroup(
    name: String,
    private val groupDepth: Int,
    private vararg val comment: String?,
    private val getBaseDepth: () -> Int,
    private val onBaseDepthUpdate: (Int) -> Unit
) : CValue<Boolean?, ForgeConfigSpec.BooleanValue?>(
    name = name,
    provider = IValueProvider { _: Builder? -> null },
    comment = comment
) {
    override fun register(builder: Builder) {
        var depth = getBaseDepth.invoke()
        if (depth > groupDepth) builder.pop(depth - groupDepth)
        depth = groupDepth
        addComments(builder, *comment)
        builder.push(this.name)
        onBaseDepthUpdate.invoke(depth + 1)
    }
}