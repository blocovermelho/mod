package org.blocovermelho.bvauth.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import kotlinx.coroutines.launch
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import org.blocovermelho.bvauth.BvAuthMod
import org.blocovermelho.bvauth.api.routes.rProfile
import org.blocovermelho.bvauth.api.types.PasswordUpdate
import org.blocovermelho.bvauth.ext.Colors
import org.blocovermelho.bvauth.ext.Core.colorize
import org.blocovermelho.bvauth.ext.Core.toLiteral
import org.blocovermelho.bvauth.ext.Headers
import org.blocovermelho.bvauth.ext.dsl.*
import org.blocovermelho.bvauth.ext.message
import org.blocovermelho.bvauth.impl.CoroutineManager
import org.blocovermelho.bvauth.impl.Err
import org.blocovermelho.bvauth.impl.Ok

object ChangePassword {
    fun register(
        dispatcher: CommandDispatcher<CommandSourceStack>,
    ) {
        val root = Commands.literal("mudarsenha").requires {
            it.isPlayer && BvAuthMod.LoggedUsers.contains(it.player!!.uuid) && BvAuthMod.KnownProfiles.containsKey(it.player!!.uuid)
        }.then(
                Commands.argument("antiga", StringArgumentType.word())
                    .then(Commands.argument("nova", StringArgumentType.word()).executes {
                        val player = it.source.player!!
                        val profile = BvAuthMod.KnownProfiles[player.uuid]!!
                        val antiga = StringArgumentType.getString(it, "antiga")
                        val nova = StringArgumentType.getString(it, "nova")

                        CoroutineManager.scope.launch {
                            when (val result = rProfile.PasswordChange(profile.username, antiga, nova)) {
                                is Ok -> {
                                    val text = when (result.value) {
                                        PasswordUpdate.InvalidPassword -> buildLine {
                                            this += "Senha incorreta.".colorize(Colors.ERR)
                                            this += " Verifique se a senha antiga bate com a que você logou."
                                        }
                                        PasswordUpdate.InvalidPlayerState -> "Estado do jogador inválido.".toLiteral()
                                        PasswordUpdate.PasswordChanged -> "Senha alterada.".colorize(Colors.COMMAND_GREEN)
                                        PasswordUpdate.ProfileNotInServer -> "Perfil não presente no servidor.".toLiteral()
                                        PasswordUpdate.ServerOffline -> "Servidor offline.".toLiteral()
                                    }

                                    player.sendSystemMessage(
                                        buildLine(Headers.Server, Headers.ChangePw, text)
                                    )
                                }

                                is Err -> {
                                    player.sendSystemMessage(
                                        buildLine(Headers.Server, Headers.ChangePw, result.message("a mudança da sua senha."))
                                    )
                                }
                            }
                        }

                        1
                    })
            )


        dispatcher.register(root)
    }
}