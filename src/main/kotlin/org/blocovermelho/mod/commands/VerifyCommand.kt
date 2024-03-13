package org.blocovermelho.mod.commands

import com.mojang.brigadier.CommandDispatcher
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import org.blocovermelho.mod.BVQuilt
import org.blocovermelho.mod.api.ws.SocketEvent
import org.blocovermelho.mod.api.ws.SocketEventBuilders
import org.blocovermelho.mod.ext.isBypassing
import org.blocovermelho.mod.ext.isLogged
import org.blocovermelho.mod.ext.launch
import org.quiltmc.qkl.library.brigadier.argument.greedyString
import org.quiltmc.qkl.library.brigadier.argument.value
import org.quiltmc.qkl.library.brigadier.register
import org.quiltmc.qkl.library.brigadier.required
import org.quiltmc.qkl.library.brigadier.util.player
import org.quiltmc.qkl.library.brigadier.util.sendFeedback

object VerifyCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register("verificar"){
            requires { it.isPlayer && !it.player!!.isLogged() && it.player!!.isBypassing() }
            launch {
                sendFeedback { Text.of("Yeah sure why not.") }
            }
            required(greedyString("token")) { token ->
                launch {
                    val otp = token().value().trim()
                    val awk = SocketEventBuilders.CIDR.awk(otp)
                    BVQuilt.Store.BypassCidrCheck[this.player!!.uuid] = otp
                    BVQuilt.Store.Channels.Outgoing.Messages.send(awk)
                    sendFeedback {
                        Text.of("Verificação em andamento.")
                    }
                }
            }
        }
    }
}
