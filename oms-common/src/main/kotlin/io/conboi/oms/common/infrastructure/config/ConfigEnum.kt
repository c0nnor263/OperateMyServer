package io.conboi.oms.common.infrastructure.config

import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.common.ForgeConfigSpec.Builder

class ConfigEnum<T : Enum<T>>(
    name: String,
    defaultValue: T,
    comment: Array<out String?>
) : CValue<T, ForgeConfigSpec.EnumValue<T>>(
    name = name,
    provider = IValueProvider { builder: Builder ->
        builder.defineEnum<T>(
            name, defaultValue
        )
    },
    comment = comment
)