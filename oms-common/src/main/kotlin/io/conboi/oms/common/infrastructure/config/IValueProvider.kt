package io.conboi.oms.common.infrastructure.config

import java.util.function.Function
import net.minecraftforge.common.ForgeConfigSpec

fun interface IValueProvider<V, T : ForgeConfigSpec.ConfigValue<V>?> : Function<ForgeConfigSpec.Builder, T>