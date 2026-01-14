package org.blocovermelho.bvauth.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import kotlinx.coroutines.launch
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import org.blocovermelho.bvauth.BvAuthMod
import org.blocovermelho.bvauth.api.routes.rProfile
import org.blocovermelho.bvauth.api.types.Authenticate
import org.blocovermelho.bvauth.ext.Colors
import org.blocovermelho.bvauth.ext.Core.colorize
import org.blocovermelho.bvauth.ext.Core.toLiteral
import org.blocovermelho.bvauth.ext.Headers
import org.blocovermelho.bvauth.ext.dsl.*
import org.blocovermelho.bvauth.ext.message
import org.blocovermelho.bvauth.impl.CoroutineManager
import org.blocovermelho.bvauth.impl.Err
import org.blocovermelho.bvauth.impl.Ok

object cLogin {
    fun register(
        dispatcher: CommandDispatcher<CommandSourceStack>,
    ) {
        val root = Commands.literal("login").requires { it.isPlayer &&
                !BvAuthMod.LoggedUsers.contains(it.player!!.uuid)
                && BvAuthMod.KnownProfiles.containsKey(it.player!!.uuid)
        }
            .then(Commands.argument("senha", StringArgumentType.greedyString())
                .executes {
                val player = it.source.player!!
                val server = it.source.server!!
                val profile = BvAuthMod.KnownProfiles[player.uuid]!!
                val token = StringArgumentType.getString(it, "senha").trim()

                CoroutineManager.scope.launch {
                    val auth = rProfile.Authenticate(profile.username, player.ipAddress, token)
                    when (auth) {
                        is Ok -> {
                            val text = when (auth.value) {
                                is Authenticate.InvalidPassword -> "Senha Inválida.".colorize(Colors.ERR)
                                Authenticate.InvalidProfile -> "Pefil Inválido.".colorize(Color.YELLOW)
                                Authenticate.LoggedIn ->  {
                                    BvAuthMod.LoggedUsers.add(player.uuid)
                                    player.setGameMode(BvAuthMod.Config.Gamemode)
                                    server.commands.sendCommands(player)
                                    "Logado com sucesso.".colorize(Colors.COMMAND_GREEN)
                                }
                                Authenticate.ServerOffline -> "Servidor Offline.".toLiteral()
                            }

                            player.sendSystemMessage(
                                buildLine(Headers.Server, Headers.Login, text)
                            )
                        }
                        is Err -> {
                            player.sendSystemMessage(
                                buildLine(Headers.Server, Headers.Login, auth.message("a autenticação com a sua conta.", "Tente novamente."))
                            )
                        }
                    }
                }

                1
            })


        dispatcher.register(root)
    }
}