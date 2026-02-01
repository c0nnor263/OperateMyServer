package io.conboi.oms.common.infrastructure.config

import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.common.ForgeConfigSpec.Builder

class ConfigInt(
    name: String,
    current: Int,
    min: Int,
    max: Int,
    vararg comment: String?
) : CValue<Int, ForgeConfigSpec.IntValue>(
    name = name,
    provider = IValueProvider { builder: Builder ->
        builder.defineInRange(
            name, current, min, max
        )
    },
    comment = comment
)