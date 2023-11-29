package org.blocovermelho.mod.commands.admin

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.server.command.ServerCommandSource
import org.blocovermelho.mod.api.Routes
import org.blocovermelho.mod.api.handleErr
import org.blocovermelho.mod.ext.Helpers.command
import org.blocovermelho.mod.ext.Helpers.maskedClipboard
import org.blocovermelho.mod.ext.Other.serverHeader
import org.blocovermelho.mod.ext.launch
import org.blocovermelho.mod.ext.sendError
import org.quiltmc.qkl.library.brigadier.argument.*
import org.quiltmc.qkl.library.brigadier.register
import org.quiltmc.qkl.library.brigadier.required
import org.quiltmc.qkl.library.brigadier.util.player
import org.quiltmc.qkl.library.brigadier.util.sendFeedback
import org.quiltmc.qkl.library.text.Color
import org.quiltmc.qkl.library.text.buildText
import org.quiltmc.qkl.library.text.color
import org.quiltmc.qkl.library.text.literal


object Revoke {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register("bvadmin") {
            //TODO: Luckperms integration
            //Replace OP Check for luckperms api check.
            requires { it.isPlayer && it.player!!.hasPermissionLevel(2) }
            required(literal("revoke")) { _ ->
                required(enum("type", listOf("LINK", "ACCOUNT"))) { kind ->
                    required(player("user")) { user ->
                        launch {
                            val kindVal = kind().value()
                            val target = user().value()

                            if ( kindVal.uppercase() == "LINK") {
                                val account = Routes.User.Exists(target.uuid).handleErr { sendError(it, "verificando se o link existe") }
                                    ?: return@launch

                                if (!account) {
                                    sendFeedback {
                                        buildText {
                                            serverHeader {
                                                literal(" O jogador ")
                                                color(Color.YELLOW) {
                                                    literal(target.gameProfile.name)
                                                }
                                                literal(" não consta no sistema.")
                                            }
                                        }
                                    }
                                }

                                val delete = Routes.User.Delete(target.uuid).handleErr { sendError(it, "apagando o link") }
                                    ?: return@launch

                                target.sendSystemMessage(buildText {
                                    serverHeader {
                                        literal(" O Link com o seu discord foi apagado com sucesso.\n")
                                        literal(" Staff responsável: ")
                                        color(Color.GREY) {
                                            literal("${player!!.gameProfile.name}\n")
                                        }

                                        literal(" Clique na ")
                                        maskedClipboard("<@${delete.discordId}>","menção da conta")
                                        literal(" para verificar qual era a conta que estava linkado à sua previamente.\n")
                                    }

                                    serverHeader {
                                        literal(" Use ")
                                        command("/link")
                                        literal(" para linkar sua outra conta.")
                                    }
                                })

                                sendFeedback {
                                    buildText {
                                        serverHeader {
                                            literal(" Link de ")
                                            color(Color.YELLOW) {
                                                literal(target.gameProfile.name)
                                            }
                                            literal(" apagado com sucesso.")
                                        }
                                    }
                                }

                            } else if ( kindVal.uppercase() == "ACCOUNT") {
                                val account = Routes.Auth.Exists(target.uuid).handleErr { sendError(it, "verificando se a conta existe") }
                                    ?: return@launch


                                if (!account) {
                                    sendFeedback {
                                        buildText {
                                            serverHeader {
                                                literal(" O jogador ")
                                                color(Color.YELLOW) {
                                                    literal(target.gameProfile.name)
                                                }
                                                literal(" não consta no sistema.")
                                            }
                                        }
                                    }
                                }

                                Routes.Auth.Delete(target.uuid).handleErr { sendError(it, "apagando a conta") }
                                    ?: return@launch

                                target.sendSystemMessage(buildText {
                                    serverHeader {
                                        literal(" A sua conta foi apagada com sucesso.\n")
                                        literal(" Staff responsável: ")
                                        color(Color.GREY) {
                                            literal("${player!!.gameProfile.name}\n")
                                        }
                                    }

                                    serverHeader {
                                        literal(" Use ")
                                        command("/registrar")
                                        literal(" para criar uma nova senha para sua conta.")
                                    }
                                })

                                sendFeedback {
                                    buildText {
                                        serverHeader {
                                            literal(" Conta de ")
                                            color(Color.YELLOW) {
                                                literal(target.gameProfile.name)
                                            }
                                            literal(" apagado com sucesso.")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
    }
}
