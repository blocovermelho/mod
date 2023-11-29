package org.blocovermelho.mod.commands

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.server.command.ServerCommandSource
import org.blocovermelho.mod.BVQuilt
import org.blocovermelho.mod.BVQuilt.Store.ServerUUID
import org.blocovermelho.mod.api.Routes
import org.blocovermelho.mod.ext.*
import org.blocovermelho.mod.api.handleErr
import org.blocovermelho.mod.api.models.LoginRequest
import org.blocovermelho.mod.ext.launch
import org.blocovermelho.mod.ext.Commands.login
import org.blocovermelho.mod.ext.Helpers.command
import org.quiltmc.qkl.library.brigadier.argument.value
import org.quiltmc.qkl.library.brigadier.argument.word
import org.quiltmc.qkl.library.brigadier.register
import org.quiltmc.qkl.library.brigadier.required
import org.quiltmc.qkl.library.brigadier.util.sendFeedback
import org.quiltmc.qkl.library.text.*

object LoginCommand {
     fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register("login") {
            requires { it.isPlayer && !it.player!!.isLogged() }
            required(word("senha")) { password ->
                launch {
                    val player = this.source.player!!
                    val pwd = password().value()
                    val user = this.source.player!!

                    val hasAccount = Routes.Auth.Exists(user.uuid).handleErr { sendError(it, "obtenção da sua conta") }
                        ?: return@launch

                    if (!hasAccount) {
                        sendFeedback {
                            buildText {
                                login {
                                    literal(" Você ainda não foi registrado.\n")
                                    literal(" Use o comando ")
                                    command("/registrar")
                                    literal(" para registrar uma senha.")
                                }
                            }
                        }
                        return@launch
                    }

                    val correct = Routes.Auth.Login(ServerUUID, LoginRequest(user.uuid, user.ip, pwd)).handleErr {
                        sendError(it, "verificação do seu login")
                    } ?: return@launch

                    if (!correct) {
                        sendFeedback { buildText { login { literal(" Senha incorreta.") } } }
                        return@launch
                    }

                    BVQuilt.Store.LoggedPlayers.add(player.uuid)
                    player.changeGameMode(BVQuilt.SERVER_DATA.postLoginGamemode.value())

                    player.updateCommandTree()
                    sendFeedback { buildText { login { literal(" Logado com sucesso.") } } }
                }
            }
        }
    }

}
