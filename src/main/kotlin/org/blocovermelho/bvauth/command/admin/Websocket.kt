package org.blocovermelho.bvauth.command.admin

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

object Websocket {
    fun register(
        dispatcher: CommandDispatcher<CommandSourceStack>,
        ctx: CommandBuildContext,
        where: Commands.CommandSelection
    ) {

    }
}