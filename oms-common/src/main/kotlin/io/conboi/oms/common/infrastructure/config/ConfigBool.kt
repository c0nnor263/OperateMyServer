package io.conboi.oms.common.infrastructure.config

import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.common.ForgeConfigSpec.Builder

class ConfigBool(
    name: String,
    def: Boolean,
    vararg comment: String?
) : CValue<Boolean, ForgeConfigSpec.BooleanValue>(
    name = name,
    provider = IValueProvider { builder: Builder ->
        builder.define(name, def)
    },
    comment = comment
)