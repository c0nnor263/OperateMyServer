package io.conboi.oms.api.infrastructure.config

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class FeatureConfigTest : ShouldSpec({

    class TestFeatureConfig(
        override val name: String,
        private var enabled: Boolean,
        private val data: Map<String, Any> = emptyMap()
    ) : FeatureConfig {

        override fun isEnabled(): Boolean = enabled

        override fun enable() {
            enabled = true
        }

        override fun disable() {
            enabled = false
        }

        override fun getConfigData(): Map<String, Any> = data
    }

    should("return correct FeatureConfigInfo snapshot") {
        val config = TestFeatureConfig(
            name = "test_feature",
            enabled = true,
            data = mapOf("a" to 1)
        )

        val info = config.info()

        info.name shouldBe "test_feature"
        info.isEnabled shouldBe true
        info.data shouldBe mapOf("a" to 1)
    }

    should("reflect enable/disable state changes") {
        val config = TestFeatureConfig(
            name = "test_feature",
            enabled = false
        )

        config.isEnabled() shouldBe false

        config.enable()
        config.isEnabled() shouldBe true

        config.disable()
        config.isEnabled() shouldBe false
    }

    should("return empty data map by default") {
        val config = TestFeatureConfig(
            name = "test_feature",
            enabled = true
        )

        config.getConfigData() shouldBe emptyMap()
        config.info().data shouldBe emptyMap()
    }
})

