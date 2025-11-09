package io.conboi.oms.elements.commands.feature

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import io.conboi.oms.api.OMSFeatureManagers
import io.conboi.oms.api.elements.commands.OMSCommandEntry
import io.conboi.oms.api.foundation.feature.FeatureInfo
import io.conboi.oms.api.foundation.feature.OmsFeature
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import net.minecraft.commands.CommandSourceStack

class FeatureCommandTest : FunSpec({

    test("featureCommand should include dummy command for each feature") {
        val dummyCommand = object : OMSCommandEntry() {
            override fun init() =
                com.mojang.brigadier.builder.LiteralArgumentBuilder.literal<CommandSourceStack>("dummy")
        }

        val feature = object : OmsFeature<Nothing>() {
            override val info: FeatureInfo = FeatureInfo("testmod:testfeature", FeatureInfo.Priority.NONE)
            override fun getFeatureCommands(): List<OMSCommandEntry> = listOf(dummyCommand)
        }

        mockkObject(OMSFeatureManagers)
        every { OMSFeatureManagers.oms.prioritizedFeatures } returns listOf(feature)

        val dispatcher = CommandDispatcher<CommandSourceStack>()
        val root = FeatureCommand().build() as LiteralArgumentBuilder<CommandSourceStack>
        dispatcher.register(root)

        val featureNode =
            dispatcher.root.children.find { it.name == "feature" } as? LiteralCommandNode<CommandSourceStack>
        featureNode shouldNotBe null

        val featureIdNode =
            featureNode!!.children.find { it.name == "testmod:testfeature" } as? LiteralCommandNode<CommandSourceStack>
        featureIdNode shouldNotBe null

        val dummyNode = featureIdNode!!.children.find { it.name == "dummy" }
        dummyNode shouldNotBe null

        unmockkObject(OMSFeatureManagers)
    }
})