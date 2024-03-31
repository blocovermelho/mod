package org.blocovermelho.mod.commands.admin

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.server.command.ServerCommandSource
import org.blocovermelho.mod.ext.isLogged
import org.quiltmc.qkl.library.brigadier.register

object AdminCommands {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register("bvadmin") {
            //TODO: Luckperms integration
            //Replace OP Check for luckperms api check.
            requires { it.isPlayer && it.player!!.hasPermissionLevel(2) && it.player!!.isLogged() }
            revokeCommand()
            unlock()
        }
    }
}
