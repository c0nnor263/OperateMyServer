package io.conboi.oms.common.infrastructure.config

import java.util.function.Predicate
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue

class ConfigString(
    name: String,
    def: String,
    private val validator: Predicate<String?>,
    vararg comment: String?
) : CValue<String, ConfigValue<String>?>(
    name = name,
    provider = IValueProvider { builder ->
        val safeValidator = Predicate<Any?> { it is String && validator.test(it) }
        builder.define(name, def, safeValidator) as ConfigValue<String>
    },
    comment = comment
)