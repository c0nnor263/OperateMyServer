package io.conboi.oms.api.elements.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.commands.CommandSourceStack

/**
 * Base class for defining a command branch within the OMS command system.
 *
 * A command branch represents a logical group of related commands
 * registered under a shared root literal, separate from the main `/oms` command.
 *
 * Example usage:
 * ```
 * /<branch> <command>
 * ```
 *
 * Concrete implementations are expected to provide a list of
 * [OMSCommandEntry] instances that define the actual commands
 * belonging to this branch.
 */
abstract class OMSCommandBranch {
    /**
     * Returns the list of command entries belonging to this branch.
     *
     * Each [OMSCommandEntry] is responsible for building its own
     * Brigadier command structure.
     *
     * @return a list of command entries registered under this branch
     */
    protected abstract fun getCommands(): List<OMSCommandEntry>

    /**
     * Registers all commands of this branch into the given command dispatcher.
     *
     * If a [groupBuilder] is provided, all commands are attached as subcommands
     * of that literal node, and the group is then registered in the dispatcher.
     *
     * If [groupBuilder] is `null`, no registration is performed.
     *
     * @param dispatcher the Brigadier command dispatcher
     * @param groupBuilder optional root literal used to group commands
     */
    open fun register(
        dispatcher: CommandDispatcher<CommandSourceStack>,
        groupBuilder: LiteralArgumentBuilder<CommandSourceStack>? = null
    ) {
        groupBuilder?.let { builder ->
            getCommands().forEach { command ->
                val commandBuilder = command.build()
                builder.then(commandBuilder)
            }
            dispatcher.register(builder)
        }
    }
}