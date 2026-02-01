package io.conboi.oms.api.foundation.feature

import io.conboi.oms.api.foundation.CachedField
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.verify

class ConfigWatcherTest : ShouldSpec({

    lateinit var sut: TestConfigWatcher

    beforeEach {
        sut = TestConfigWatcher()
    }

    should("flag configuration as dirty") {
        sut.isConfigDirty.shouldBeFalse()
        sut.markConfigAsDirty()
        sut.isConfigDirty.shouldBeTrue()
    }

    should("manual flag configuration as dirty") {
        sut.isConfigDirty.shouldBeFalse()
        sut.changeConfigDirty(true)
        sut.isConfigDirty.shouldBeTrue()
    }

    should("reset configuration updated flag when onConfigUpdated is invoked") {
        sut.markConfigAsDirty()
        sut.isConfigDirty.shouldBeTrue()

        sut.onConfigUpdated(mockk())
        sut.isConfigDirty.shouldBeFalse()
    }

    should("call watch on all registered config fields") {
        val field1 = mockk<CachedField<String, Int>>(relaxed = true)
        val field2 = mockk<CachedField<String, String>>(relaxed = true)

        val list = ConfigWatcher::class.java
            .getDeclaredField("configFields")
            .apply { isAccessible = true }
            .get(sut) as MutableList<CachedField<*, *>>

        list.add(field1)
        list.add(field2)

        sut.exposeWatchConfig()

        verify { field1.watch() }
        verify { field2.watch() }
    }

    should("create configField with observe=true and register it") {
        var callCount = 0

        val field = sut.addField {
            key = { "k" }
            value = { ++callCount }
        }

        field.observe shouldBe true
        field.get() shouldBe 1

        val fieldsList = ConfigWatcher::class.java
            .getDeclaredField("configFields")
            .apply { isAccessible = true }
            .get(sut) as List<*>

        fieldsList.contains(field) shouldBe true
    }
})

class TestConfigWatcher : ConfigWatcher() {
    fun exposeWatchConfig() = watchConfig()
    fun changeConfigDirty(value: Boolean) {
        isConfigDirty = value
    }

    fun <K, V> addField(block: CachedField.Builder<K, V>.() -> Unit): CachedField<K, V> {
        return configField(block)
    }
}
