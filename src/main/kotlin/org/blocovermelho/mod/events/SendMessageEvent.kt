package org.blocovermelho.mod.events

import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.ActionResult
import org.blocovermelho.mod.ext.isLogged

val validCommands = listOf("login", "link", "registrar")

fun onPlayerChat(spe: ServerPlayerEntity, message: String) : ActionResult {
    if(spe.isLogged()) {
        return ActionResult.PASS
    }

    if (validCommands.contains(message.split(" ")[0].lowercase())) {
        return ActionResult.PASS
    }

    return ActionResult.FAIL
}
