package org.blocovermelho.mod.commands.admin

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.server.command.ServerCommandSource

object AdminCommands {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        Revoke.register(dispatcher)
    }
}
