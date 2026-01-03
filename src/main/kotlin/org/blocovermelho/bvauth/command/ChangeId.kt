package org.blocovermelho.bvauth.command

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.UuidArgument
import net.minecraft.network.chat.Component
import org.blocovermelho.bvauth.impl.IdSwapper
import java.util.*

object ChangeId {
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        val root = Commands.literal("swapid").requires { it.isPlayer }
        val random = Commands.literal("random").executes {
            val player = it.source.player!!
            val pl = it.source.server.playerList!!
            val rnd = UUID.randomUUID()
            val old = player.uuid

            player.uuid = rnd


            (pl as IdSwapper).`bv$swapId`(old, rnd)

            it.source.sendSuccess(
                { Component.literal("[Random] Your uuid should have changed from `$old` to `$rnd`") },
                false
            )

            1
        }

        val set = Commands.literal("set").then(Commands.argument("id", UuidArgument.uuid()).executes {
            val new = it.getArgument("id", UUID::class.java)!!

            val player = it.source.player!!
            val pl = it.source.server.playerList!!
            val old = player.uuid

            (pl as IdSwapper).`bv$swapId`(old, new)


            it.source.sendSuccess({ Component.literal("[Random] You've set your uuid to `$new`") }, false)

            1
        })

        dispatcher.register(root.then(random).then(set))
    }
}