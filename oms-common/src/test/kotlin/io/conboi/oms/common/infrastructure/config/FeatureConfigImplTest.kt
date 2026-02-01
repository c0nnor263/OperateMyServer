package io.conboi.oms.common.infrastructure.config

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.maps.shouldContainKeys
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import net.minecraftforge.common.ForgeConfigSpec

class FeatureConfigImplTest : ShouldSpec({

    class TestFeatureConfig : FeatureConfigImpl() {
        override val name: String = "test_feature"

        val count = i(5, 0, 10, "count")
        val text = s("abc", "text")
    }

    fun register(cfg: ConfigBase) {
        val builder = mockk<ForgeConfigSpec.Builder>(relaxed = true)
        cfg.registerAll(builder)
    }


    should("register enabled flag and custom fields") {
        val cfg = TestFeatureConfig()
        register(cfg)

        val data = cfg.getConfigData()

        data.shouldContainKeys(
            "enabled",
            "count",
            "text"
        )
    }

    should("reflect field presence after mutations") {
        val cfg = TestFeatureConfig()
        register(cfg)

        cfg.enable()
        cfg.disable()
        cfg.count.set(9)
        cfg.text.set("xyz")

        val data = cfg.getConfigData()

        data.shouldContainKeys(
            "enabled",
            "count",
            "text"
        )
    }

    should("exclude ConfigGroup entries from config data") {
        class GroupedConfig : FeatureConfigImpl() {
            override val name = "grouped"

            val group = group(1, "group")
            val value = i(3, "value")
        }

        val cfg = GroupedConfig()
        register(cfg)

        val data = cfg.getConfigData()

        data.shouldContainKeys(
            "enabled",
            "value"
        )
        data.containsKey("group") shouldBe false
    }
})
