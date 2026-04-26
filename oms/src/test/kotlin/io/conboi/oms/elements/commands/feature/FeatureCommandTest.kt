package io.conboi.oms.elements.commands.feature

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import io.conboi.oms.OmsAddons
import io.conboi.oms.api.elements.commands.OMSCommandEntry
import io.conboi.oms.api.foundation.feature.FeatureInfo
import io.conboi.oms.api.foundation.feature.Priority
import io.conboi.oms.api.foundation.manager.FeatureManagerInfo
import io.conboi.oms.foundation.addon.AddonInstance
import io.conboi.oms.foundation.addon.OmsAddonInfo
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

    // Fake addons
    val mockAddonA = mockk<AddonInstance>(relaxed = true)
    val mockAddonB = mockk<AddonInstance>(relaxed = true)

    fun feature(id: String, hasDummy: Boolean): FeatureInfo =
        FeatureInfo(
            id = id,
            priority = Priority.COMMON,
            data = emptyMap(),
            configInfo = null,
            additionalCommands = if (hasDummy)
                listOf(DummyCommand())
            else emptyList()
        )

    fun manager(addonId: String, features: List<FeatureInfo>) =
        FeatureManagerInfo(
            id = "$addonId:main",
            addonId = addonId,
            name = "$addonId:main",
            data = emptyMap(),
            featuresInfo = features
        )

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
        every { mockAddonA.info() } returns OmsAddonInfo(
            id = "modone",
            featureManagerInfo = manager(
                "modone",
                listOf(
                    feature("fea", hasDummy = true),
                    feature("feb", hasDummy = false),
                )
            )
        )

        every { mockAddonB.info() } returns OmsAddonInfo(
            id = "modtwo",
            featureManagerInfo = manager(
                "modtwo",
                listOf(
                    feature("fex", hasDummy = true),
                    feature("fey", hasDummy = true),
                )
            )
        )

        every { OmsAddons.forEachInstance(any()) } answers {
            val callback = firstArg<(AddonInstance) -> Unit>()
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

class DummyCommand : OMSCommandEntry() {
    override fun init(): LiteralArgumentBuilder<CommandSourceStack> =
        Commands.literal("dummy")
}