package io.conboi.oms.common.infrastructure.config

import java.util.function.Predicate
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue

class ConfigList<T>(
    name: String,
    def: List<T>,
    private val validator: Predicate<T?>,
    vararg comment: String?
) : CValue<List<T>, ConfigValue<List<T>>?>(
    name = name,
    provider = IValueProvider<List<T>, ConfigValue<List<T>>?> { builder ->
        val safeValidator = Predicate<Any?> { it == null || validator.test(it as? T) }
        builder.defineList(name, def, safeValidator) as ConfigValue<List<T>>
    },
    comment = comment
)