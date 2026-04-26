package io.conboi.oms.common.infrastructure.config

import net.minecraftforge.common.ForgeConfigSpec.Builder
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue

open class CValue<V, T : ConfigValue<V>?>(
    var name: String,
    provider: IValueProvider<V, T?>,
    vararg comment: String?
) {
    protected var value: ConfigValue<V>? = null
    private val provider: IValueProvider<V, T?> = IValueProvider { builder: Builder ->
        addComments(builder, *comment)
        provider.apply(builder)
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