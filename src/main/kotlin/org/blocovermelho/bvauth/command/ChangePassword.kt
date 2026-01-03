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
import org.blocovermelho.bvauth.ext.Headers
import org.blocovermelho.bvauth.ext.STFBuilder.asComponent
import org.blocovermelho.bvauth.ext.STFBuilder.color
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
                                        PasswordUpdate.InvalidPassword -> "Senha incorreta.".color(Colors.ERR) + " Verifique se a senha antiga bate com a que você logou."
                                        PasswordUpdate.InvalidPlayerState -> "Estado do jogador inválido."
                                        PasswordUpdate.PasswordChanged -> "Senha alterada.".color(Colors.COMMAND_GREEN)
                                        PasswordUpdate.ProfileNotInServer -> "Perfil não presente no servidor."
                                        PasswordUpdate.ServerOffline -> "Servidor offline."
                                    }

                                    player.sendSystemMessage(
                                        listOf(
                                            Headers.Server, Headers.ChangePw, text
                                        ).joinToString(" ").asComponent()
                                    )
                                }

                                is Err -> {
                                    player.sendSystemMessage(
                                        listOf(
                                            Headers.Server, Headers.ChangePw, result.message("a mudança da sua senha.")
                                        ).joinToString(" ").asComponent()
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