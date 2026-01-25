package io.conboi.oms.common.infrastructure.config

import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.common.ForgeConfigSpec.Builder

class ConfigFloat(
    name: String,
    current: Float,
    min: Float,
    max: Float,
    vararg comment: String?
) : CValue<Double, ForgeConfigSpec.DoubleValue>(
    name = name,
    provider = IValueProvider { builder: Builder ->
        builder.defineInRange(
            name, current.toDouble(), min.toDouble(), max.toDouble()
        )
    },
    comment = comment
)