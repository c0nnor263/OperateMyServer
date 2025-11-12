package io.conboi.oms.api.foundation.feature

import io.conboi.oms.api.foundation.CachedField
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.verify

class ConfigWatcherTest : FunSpec({

    class TestConfigWatcher : ConfigWatcher() {
        fun exposeWatchConfig() = watchConfig()
        fun <K, V> addField(block: CachedField.Builder<K, V>.() -> Unit): CachedField<K, V> {
            return configField(block)
        }
    }

    lateinit var watcher: TestConfigWatcher

    beforeTest {
        watcher = TestConfigWatcher()
    }

    test("should flag config as dirty") {
        watcher.isConfigurationUpdated.shouldBeFalse()
        watcher.flagConfigAsDirty()
        watcher.isConfigurationUpdated.shouldBeTrue()
    }

    test("should reset config update flag on onConfigUpdated") {
        watcher.flagConfigAsDirty()
        watcher.isConfigurationUpdated.shouldBeTrue()

        watcher.onConfigUpdated(mockk())
        watcher.isConfigurationUpdated.shouldBeFalse()
    }

    test("should watch all configFields") {
        val field1 = mockk<CachedField<String, Int>>(relaxed = true)
        val field2 = mockk<CachedField<String, String>>(relaxed = true)

        val configFields = ConfigWatcher::class.java
            .getDeclaredField("configFields")
            .apply { isAccessible = true }
            .get(watcher) as MutableList<CachedField<*, *>>

        configFields.addAll(listOf(field1, field2))

        watcher.exposeWatchConfig()

        verify { field1.watch() }
        verify { field2.watch() }
    }

    test("configField should create and register observed field") {
        val field = watcher.addField {
            key = { "config-key" }
            value = { 123 }
        }

        field.get() shouldBe 123
        field.observe shouldBe true
    }
})
