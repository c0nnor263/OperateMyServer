package io.conboi.oms.elements.commands.feature

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import io.conboi.oms.api.OmsAddons
import io.conboi.oms.api.elements.commands.OMSCommandEntry
import io.conboi.oms.api.foundation.addon.OmsAddon
import io.conboi.oms.api.foundation.feature.FeatureInfo
import io.conboi.oms.api.foundation.feature.OmsFeature
import io.conboi.oms.api.foundation.feature.Priority
import io.conboi.oms.api.foundation.manager.FeatureManager
import io.conboi.oms.api.infrastructure.config.ConfigProvider
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands


class FeatureCommandTest : ShouldSpec({

    lateinit var sut: FeatureCommand

    val dummyCommand = object : OMSCommandEntry() {
        override fun init(): LiteralArgumentBuilder<CommandSourceStack> =
            Commands.literal("dummy")
    }

    val mockConfigProvider = ConfigProvider { null as Nothing }

    // FEATURES FOR MOD 1
    val mod1FeatureA = object : OmsFeature<Nothing>(mockConfigProvider) {
        override fun info() = FeatureInfo(
            id = "fea",
            priority = Priority.NONE,
            commands = listOf(dummyCommand)
        )
    }

    val mod1FeatureB = object : OmsFeature<Nothing>(mockConfigProvider) {
        override fun info() = FeatureInfo(
            id = "feb",
            priority = Priority.NONE,
            commands = emptyList()
        )
    }

    // FEATURES FOR MOD 2
    val mod2FeatureX = object : OmsFeature<Nothing>(mockConfigProvider) {
        override fun info() = FeatureInfo(
            id = "fex",
            priority = Priority.NONE,
            commands = listOf(dummyCommand)
        )
    }

    val mod2FeatureY = object : OmsFeature<Nothing>(mockConfigProvider) {
        override fun info() = FeatureInfo(
            id = "fey",
            priority = Priority.NONE,
            commands = listOf(dummyCommand)
        )
    }

    // MANAGERS
    val manager1 = object : FeatureManager("modone") {
        init {
            register(mod1FeatureA)
            register(mod1FeatureB)
            freeze()
        }
    }

    val manager2 = object : FeatureManager("modtwo") {
        init {
            register(mod2FeatureX)
            register(mod2FeatureY)
            freeze()
        }
    }

    // Fake addons
    val mockAddonA = mockk<OmsAddon>()
    val mockAddonB = mockk<OmsAddon>()

    fun checkFeatureNode(
        featureRoot: LiteralCommandNode<CommandSourceStack>,
        mod: String,
        id: String,
        hasDummy: Boolean
    ) {
        val literalName = "$mod:$id"
        val featureNode = featureRoot.children.find { it.name == literalName }
        featureNode shouldNotBe null
        if (featureNode == null) return

        if (hasDummy) {
            featureNode.children.find { it.name == "dummy" } shouldNotBe null
        }

        featureNode.children.find { it.name == "enable" } shouldNotBe null
        featureNode.children.find { it.name == "disable" } shouldNotBe null
    }

    beforeSpec {
        mockkObject(OmsAddons)
    }

    beforeEach {
        every { mockAddonA.info() } returns io.conboi.oms.api.foundation.addon.OmsAddonInfo(
            id = "addonA",
            featureManagerInfo = manager1.info()
        )

        every { mockAddonB.info() } returns io.conboi.oms.api.foundation.addon.OmsAddonInfo(
            id = "addonB",
            featureManagerInfo = manager2.info()
        )

        every { OmsAddons.forEachAddon(any()) } answers {
            val callback = firstArg<(OmsAddon) -> Unit>()
            callback(mockAddonA)
            callback(mockAddonB)
        }

        sut = FeatureCommand()
    }

    afterEach {
        clearAllMocks()
    }

    should("register feature commands for multiple managers and multiple features") {
        val dispatcher = CommandDispatcher<CommandSourceStack>()
        val root = sut.build() as LiteralArgumentBuilder<CommandSourceStack>
        dispatcher.register(root)

        val featureRoot =
            dispatcher.root.children.find { it.name == "feature" } as? LiteralCommandNode<CommandSourceStack>
        featureRoot shouldNotBe null
        if (featureRoot == null) return@should

        checkFeatureNode(featureRoot, "modone", "fea", hasDummy = true)
        checkFeatureNode(featureRoot, "modone", "feb", hasDummy = false)
        checkFeatureNode(featureRoot, "modtwo", "fex", hasDummy = true)
        checkFeatureNode(featureRoot, "modtwo", "fey", hasDummy = true)
    }
})
