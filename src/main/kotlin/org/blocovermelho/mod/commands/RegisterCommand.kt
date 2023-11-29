package org.blocovermelho.mod.commands

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.server.command.ServerCommandSource
import org.blocovermelho.mod.BVQuilt
import org.blocovermelho.mod.api.Routes
import org.blocovermelho.mod.api.handleErr
import org.blocovermelho.mod.api.models.CreateAccount
import org.blocovermelho.mod.ext.launch
import org.blocovermelho.mod.ext.*
import org.blocovermelho.mod.ext.Commands.registrar
import org.blocovermelho.mod.ext.Helpers.command
import org.quiltmc.qkl.library.brigadier.argument.value
import org.quiltmc.qkl.library.brigadier.argument.word
import org.quiltmc.qkl.library.brigadier.register
import org.quiltmc.qkl.library.brigadier.required
import org.quiltmc.qkl.library.brigadier.util.sendFeedback
import org.quiltmc.qkl.library.text.Color
import org.quiltmc.qkl.library.text.buildText
import org.quiltmc.qkl.library.text.color
import org.quiltmc.qkl.library.text.literal

object RegisterCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register("registrar") {
            requires { it.isPlayer && !it.player!!.isLogged() }
            required(word("senha")) { senha ->
                required(word("repetirSenha")) { repetirSenha ->
                    launch {
                        val pwd = senha().value()
                        val rep = repetirSenha().value()

                        if (pwd != rep) {
                            sendFeedback {
                                buildText {
                                    registrar {
                                        literal(" As senhas não são idênticas.\n")
                                        literal(" Verifique se")
                                        color(Color.YELLOW) {
                                            literal("\"$rep\"")
                                        }
                                        literal(" é igual a senha digitada.")
                                    }
                                }
                            }
                            return@launch
                        }

                        val player = this.source.player!!

                        // A user must exist before it's account is created
                        val uE = Routes.User.Exists(player.uuid).handleErr { sendError(it, "verificando sua conta") }
                            ?: return@launch

                        if (!uE) {
                            sendFeedback {
                                buildText {
                                    registrar {
                                        literal(" Você ainda não linkou sua conta do discord.\n")
                                        literal(" Use ")
                                        command("/link")
                                        literal(" para linkar sua conta do discord.")
                                    }
                                }
                            }
                            return@launch
                        }

                        Routes.Auth.Create(CreateAccount(player.uuid, pwd, player.ip)).handleErr { sendError(it, "criar sua conta") }
                            ?: return@launch


                        Routes.Auth.Resume(player.uuid).handleErr { sendError(it, "atualizando dados da sua conta") }

                        BVQuilt.Store.LoggedPlayers.add(player.uuid)
                        player.changeGameMode(BVQuilt.SERVER_DATA.postLoginGamemode.value())
                        player.updateCommandTree()

                        sendFeedback { buildText { registrar { literal("Conta criada com sucesso.") } } }
                    }
                }
            }
        }
    }
}
