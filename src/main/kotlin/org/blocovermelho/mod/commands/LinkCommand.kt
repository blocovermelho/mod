package org.blocovermelho.mod.commands

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.server.command.ServerCommandSource
import org.blocovermelho.mod.api.Routes
import org.blocovermelho.mod.api.handleErr
import org.blocovermelho.mod.api.models.CreateUser
import org.blocovermelho.mod.api.ws.messages.handleErr
import org.blocovermelho.mod.ext.Commands.link
import org.blocovermelho.mod.ext.Helpers.clipboard
import org.blocovermelho.mod.ext.Helpers.command
import org.blocovermelho.mod.ext.Helpers.maskedUri
import org.blocovermelho.mod.ext.isLogged
import org.blocovermelho.mod.ext.launch
import org.blocovermelho.mod.ext.sendAny
import org.blocovermelho.mod.ext.sendError
import org.quiltmc.qkl.library.brigadier.register
import org.quiltmc.qkl.library.brigadier.util.player
import org.quiltmc.qkl.library.brigadier.util.sendFeedback
import org.quiltmc.qkl.library.text.Color
import org.quiltmc.qkl.library.text.buildText
import org.quiltmc.qkl.library.text.color
import org.quiltmc.qkl.library.text.literal

object LinkCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register("link") {
            requires { it.isPlayer && !it.player!!.isLogged() }
            launch {
                val player = this.player!!;

                val uE = Routes.User.Exists(player.uuid).handleErr { sendError(it, "verificando se sua conta existe") }
                    ?: return@launch


                if (uE) {
                    val u =
                        Routes.User.Get(player.uuid).handleErr { sendError(it, "obtendo sua conta") } ?: return@launch

                    sendFeedback {
                        buildText {
                            link {
                                literal(" Olá ")
                                color(Color.YELLOW) {
                                    literal(u.username)
                                }
                                literal(", você já linkou sua conta do Discord.\n")
                                literal(" O seu ID do discord é: ")
                                clipboard(u.discordId)
                                literal(" caso achar que isso é um engano, entre em contato com a staff.")
                            }
                        }
                    }

                    return@launch
                }

                var link = Routes.Discord.GetLink(player.uuid).handleErr { sendError(it, "gerando o link do discord") }
                    ?: return@launch



                link = link.removeSurrounding("\"")

                sendFeedback {
                    buildText {
                        link {
                            literal(" Olá ")
                            color(Color.YELLOW) {
                                literal(player.gameProfile.name)
                            }
                            literal(". Seja bem-vinde ao Bloco Vermelho!\n")
                            maskedUri(link, "Clique aqui para linkar sua conta do discord")
                        }
                    }
                }

                val discordUser = org.blocovermelho.mod.api.ws.Routes.Discord.GetAccountForPlayer(player.uuid)
                    .handleErr { sendError(it.into(), "obtendo dados do discord") } ?: return@launch

                sendFeedback {
                    buildText {
                        link {
                            literal(" Olá ")
                            clipboard(discordUser.discordUsername)
                            literal(".\n Caso você não for essa pessoa, contate a staff.")
                        }
                    }
                }

                Routes.User.Create(CreateUser(player.uuid, player.gameProfile.name, discordUser.discordId)).handleErr { sendError(it, "criando a sua conta") } ?: return@launch

                sendFeedback {
                    buildText {
                        link {
                            literal(" Sua conta foi criada com sucesso.\n")
                            literal(" Agora, crie uma senha com ")
                            command("/registrar")
                            literal(" senha repetirSenha")
                        }
                    }
                }
            }
        }
    }
}
