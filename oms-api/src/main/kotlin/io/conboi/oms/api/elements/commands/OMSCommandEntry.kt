package io.conboi.oms.api.elements.commands

import com.mojang.brigadier.builder.ArgumentBuilder
import net.minecraft.commands.CommandSourceStack

/**
 * Base class for defining a single command entry in the OMS command system.
 *
 * A command entry represents one logical command node and may contain
 * nested subcommands, forming a hierarchical command structure.
 *
 * Example usage:
 * ```
 * /<command> <subcommand>
 * ```
 *
 * Command entries are typically registered as part of an [OMSCommandBranch].
 */
abstract class OMSCommandEntry {

    /**
     * Provides additional subcommands attached to this command entry.
     *
     * Subclasses may override this method to declare nested commands.
     * By default, no additional subcommands are defined.
     *
     * @return a list of subcommand entries
     */
    protected open fun additionalCommands(): List<OMSCommandEntry> = emptyList()

    /**
     * Initializes the base Brigadier command node for this entry.
     *
     * Implementations must define the command literal, arguments,
     * and execution logic associated with this command.
     *
     * @return the root [ArgumentBuilder] of this command entry
     */
    protected abstract fun init(): ArgumentBuilder<CommandSourceStack, *>

    /**
     * Builds the complete command tree for this entry.
     *
     * This method initializes the base command using [init] and recursively
     * attaches all subcommands provided by [additionalCommands].
     *
     * @return the fully constructed command builder
     */
    fun build(): ArgumentBuilder<CommandSourceStack, *> {
        val baseCommand = init()
        additionalCommands().forEach { additionalCommand ->
            val additionalCommandBuilder = additionalCommand.build()
            baseCommand.then(additionalCommandBuilder)
        }
        return baseCommand
    }
}