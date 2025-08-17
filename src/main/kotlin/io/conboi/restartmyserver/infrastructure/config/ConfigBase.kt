package io.conboi.restartmyserver.infrastructure.config

import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.common.ForgeConfigSpec.Builder
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import java.util.function.Supplier


abstract class ConfigBase {
    var specification: ForgeConfigSpec? = null

    protected var depth: Int = 0
    protected var allValues: MutableList<CValue<*, *>> = ArrayList<CValue<*, *>>()
    protected var children: MutableList<ConfigBase?> = ArrayList<ConfigBase?>()

    open fun registerAll(builder: Builder) {
        for (cValue in allValues) cValue.register(builder)
    }

    fun onLoad() {
        if (!children.isEmpty()) children.forEach(Consumer { obj: ConfigBase -> obj.onLoad() })
    }

    fun onReload() {
        if (!children.isEmpty()) children.forEach(Consumer { obj: ConfigBase -> obj.onReload() })
    }

    abstract val name: String

    fun interface IValueProvider<V, T : ConfigValue<V>?>
        : Function<Builder, T>

    protected fun b(current: Boolean, name: String, vararg comment: String?): ConfigBool {
        return ConfigBool(name, current, *comment)
    }

    protected fun f(current: Float, min: Float, max: Float, name: String, vararg comment: String?): ConfigFloat {
        return ConfigFloat(name, current, min, max, *comment)
    }

    protected fun f(current: Float, min: Float, name: String, vararg comment: String?): ConfigFloat {
        return f(current, min, Float.MAX_VALUE, name, *comment)
    }

    protected fun i(current: Int, min: Int, max: Int, name: String, vararg comment: String?): ConfigInt {
        return ConfigInt(name, current, min, max, *comment)
    }

    protected fun i(current: Int, min: Int, name: String, vararg comment: String?): ConfigInt {
        return i(current, min, Int.MAX_VALUE, name, *comment)
    }

    protected fun i(current: Int, name: String, vararg comment: String?): ConfigInt {
        return i(current, Int.MIN_VALUE, Int.MAX_VALUE, name, *comment)
    }

    protected fun <T : Enum<T>> e(
        defaultValue: T,
        name: String,
        vararg comment: String?
    ): ConfigEnum<T> {
        return ConfigEnum(name, defaultValue, comment)
    }

    protected fun group(depth: Int, name: String, vararg comment: String?): ConfigGroup {
        return ConfigGroup(name, depth, *comment)
    }

    protected fun <T> list(
        def: MutableList<T>,
        name: String,
        vararg comment: String,
        validator: Predicate<Any?> = Predicate { it is String }
    ): ListValue<T> = ListValue(name, def, validator, *comment)

    protected fun <T : ConfigBase> nested(depth: Int, constructor: Supplier<T>, vararg comment: String?): T {
        val config = constructor.get()
        ConfigGroup(config.name, depth, *comment)
        CValue<Boolean, ForgeConfigSpec.BooleanValue>(
            config.name,
            { builder: Builder ->
                config.depth = depth
                config.registerAll(builder)
                if (config.depth > depth) builder.pop(config.depth - depth)
                null
            })
        children.add(config)
        return config
    }

    open inner class CValue<V, T : ConfigValue<V>?>(
        var name: String,
        provider: IValueProvider<V, T?>,
        vararg comment: String?
    ) {
        protected var value: ConfigValue<V>? = null
        private val provider: IValueProvider<V, T?>

        init {
            this.provider = IValueProvider { builder: Builder ->
                addComments(builder, *comment)
                provider.apply(builder)
            }
            allValues.add(this)
        }

        fun addComments(builder: Builder, vararg comment: String?) {
            if (comment.isNotEmpty()) {
                val comments = arrayOfNulls<String>(comment.size + 1)
                comments[0] = "."
                System.arraycopy(comment, 0, comments, 1, comment.size)
                builder.comment(*comments)
            } else builder.comment(".")
        }

        open fun register(builder: Builder) {
            value = provider.apply(builder)
        }

        fun get(): V {
            value ?: throw AssertionError("Config " + this.name + " was accessed, but not registered before!")

            return value!!.get() as V
        }

        fun set(value: V?) {
            this.value ?: throw AssertionError("Config " + this.name + " was accessed, but not registered before!")
            this.value!!.set(value)
        }
    }

    //    open inner class CValue<V, T : ConfigValue<V>?>(
//        var name: String,
//        provider: IValueProvider<V, T?>,
//        vararg comment: String?
//    ) {
    inner class ListValue<T>(
        name: String,
        def: MutableList<T>,
        private val validator: Predicate<Any?>,
        vararg comment: String
    ) : CValue<MutableList<T>, ConfigValue<MutableList<T>>?>(
        name,
        IValueProvider<MutableList<T>, ConfigValue<MutableList<T>>?> { builder ->
            builder.defineList(name, def, validator) as ConfigValue<MutableList<T>>
        },
        comment = *comment
    ) {
        @Suppress("UNCHECKED_CAST")
        fun getStrings(): MutableList<String> = get() as MutableList<String>
    }

    /**
     * Marker for config subgroups
     */
    inner class ConfigGroup(name: String, private val groupDepth: Int, private vararg val comment: String?) :
        CValue<Boolean?, ForgeConfigSpec.BooleanValue?>(
            name,
            IValueProvider { _: Builder? -> null },
            *comment
        ) {

        override fun register(builder: Builder) {
            if (depth > groupDepth) builder.pop(depth - groupDepth)
            depth = groupDepth
            addComments(builder, *comment)
            builder.push(this.name)
            depth++
        }
    }

    inner class ConfigBool(name: String, def: Boolean, vararg comment: String?) :
        CValue<Boolean, ForgeConfigSpec.BooleanValue>(
            name,
            IValueProvider { builder: Builder -> builder.define(name, def) },
            *comment
        )

    inner class ConfigEnum<T : Enum<T>>(name: String, defaultValue: T, comment: Array<out String?>) :
        CValue<T, ForgeConfigSpec.EnumValue<T>>(
            name,
            IValueProvider { builder: Builder ->
                builder.defineEnum<T>(
                    name,
                    defaultValue
                )
            },
            *comment
        )

    inner class ConfigFloat(name: String, current: Float, min: Float, max: Float, vararg comment: String?) :
        CValue<Double, ForgeConfigSpec.DoubleValue>(
            name,
            IValueProvider { builder: Builder ->
                builder.defineInRange(
                    name,
                    current.toDouble(),
                    min.toDouble(),
                    max.toDouble()
                )
            },
            *comment
        ) {
        val f: Float
            get() = get().toFloat()
    }

    inner class ConfigInt(name: String, current: Int, min: Int, max: Int, vararg comment: String?) :
        CValue<Int, ForgeConfigSpec.IntValue>(
            name,
            IValueProvider { builder: Builder ->
                builder.defineInRange(
                    name,
                    current,
                    min,
                    max
                )
            },
            *comment
        )
}