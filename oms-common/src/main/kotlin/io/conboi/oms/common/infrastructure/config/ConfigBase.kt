package io.conboi.oms.common.infrastructure.config

import java.util.function.Predicate
import java.util.function.Supplier
import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.common.ForgeConfigSpec.Builder

// This configuration system is based on the code from the "Create" mod by the Create Development Team:
// Source: https://github.com/Creators-of-Create/Create
// Credits to authors of the Create mod for their robust config system design.
abstract class ConfigBase {
    var specification: ForgeConfigSpec? = null

    var baseDepth: Int = 0
    val allValues = mutableListOf<CValue<*, *>>()
    val children = mutableListOf<ConfigBase?>()

    open fun registerAll(builder: Builder) {
        allValues.forEach { cValue ->
            cValue.register(builder)
        }
    }

    abstract val name: String

    val getBaseDepth: () -> Int = { baseDepth }
    val updateBaseDepth: (Int) -> Unit = { newDepth -> baseDepth = newDepth }

    fun b(current: Boolean, name: String, vararg comment: String?): ConfigBool {
        val field = ConfigBool(name, current, *comment)
        allValues.add(field)
        return field
    }

    fun <T : Enum<T>> e(
        defaultValue: T, name: String, vararg comment: String?
    ): ConfigEnum<T> {
        val field = ConfigEnum(name, defaultValue, comment)
        allValues.add(field)
        return field
    }

    fun f(current: Float, min: Float, max: Float, name: String, vararg comment: String?): ConfigFloat {
        val field = ConfigFloat(name, current, min, max, *comment)
        allValues.add(field)
        return field
    }

    fun f(current: Float, min: Float, name: String, vararg comment: String?): ConfigFloat {
        val field = f(current, min, Float.MAX_VALUE, name, *comment)
        return field
    }

    fun group(depth: Int, name: String, vararg comment: String?): ConfigGroup {
        val field = ConfigGroup(
            name = name,
            groupDepth = depth,
            comment = comment,
            getBaseDepth = getBaseDepth,
            onBaseDepthUpdate = updateBaseDepth
        )
        allValues.add(field)
        return field
    }

    fun <T : ConfigBase> nested(depth: Int, constructor: Supplier<T>, vararg comment: String?): T {
        val config = constructor.get()
        val groupField = ConfigGroup(
            name = config.name,
            groupDepth = depth,
            comment = comment,
            getBaseDepth = getBaseDepth,
            onBaseDepthUpdate = updateBaseDepth
        )
        allValues.add(groupField)
        val field = CValue<Boolean, ForgeConfigSpec.BooleanValue>(
            config.name, { builder: Builder ->
                config.baseDepth = depth
                config.registerAll(builder)
                if (config.baseDepth > depth) builder.pop(config.baseDepth - depth)
                null
            })
        allValues.add(field)
        children.add(config)
        return config
    }

    fun i(current: Int, min: Int, max: Int, name: String, vararg comment: String?): ConfigInt {
        val field = ConfigInt(name, current, min, max, *comment)
        allValues.add(field)
        return field
    }

    fun i(current: Int, min: Int, name: String, vararg comment: String?): ConfigInt {
        return i(current, min, Int.MAX_VALUE, name, *comment)
    }

    fun i(current: Int, name: String, vararg comment: String?): ConfigInt {
        return i(current, Int.MIN_VALUE, Int.MAX_VALUE, name, *comment)
    }

    fun <T> list(
        def: List<T>, name: String,
        vararg comment: String,
        elementValidator: Predicate<T?> = Predicate { true }
    ): ConfigList<T> {
        val field = ConfigList(name, def, elementValidator, *comment)
        allValues.add(field)
        return field
    }

    fun s(
        def: String,
        name: String,
        vararg comment: String,
        validator: Predicate<String?> = Predicate { true }
    ): ConfigString {
        val field = ConfigString(name, def, validator, *comment)
        allValues.add(field)
        return field
    }

    fun normalizeValue(v: Any?): Any? =
        when (v) {
            is List<*> -> v.map { normalizeValue(it) }
            is Map<*, *> -> v.mapKeys { it.key.toString() }
                .mapValues { normalizeValue(it.value) }

            is Enum<*> -> v.name
            else -> v
        }
}