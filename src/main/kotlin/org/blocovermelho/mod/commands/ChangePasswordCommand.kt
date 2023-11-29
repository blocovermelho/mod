package org.blocovermelho.mod.commands

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.server.command.ServerCommandSource
import org.blocovermelho.mod.api.Routes
import org.blocovermelho.mod.api.handleErr
import org.blocovermelho.mod.api.models.ChangePassword
import org.blocovermelho.mod.ext.Commands.mudarSenha
import org.blocovermelho.mod.ext.isLogged
import org.blocovermelho.mod.ext.launch
import org.blocovermelho.mod.ext.sendError
import org.quiltmc.qkl.library.brigadier.argument.player
import org.quiltmc.qkl.library.brigadier.argument.value
import org.quiltmc.qkl.library.brigadier.argument.word
import org.quiltmc.qkl.library.brigadier.register
import org.quiltmc.qkl.library.brigadier.required
import org.quiltmc.qkl.library.brigadier.util.player
import org.quiltmc.qkl.library.brigadier.util.sendFeedback
import org.quiltmc.qkl.library.text.Color
import org.quiltmc.qkl.library.text.buildText
import org.quiltmc.qkl.library.text.color
import org.quiltmc.qkl.library.text.literal

object ChangePasswordCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register("mudarSenha") {
            requires { it.isPlayer && it.player!!.isLogged() }
            required(word("senhaAntiga")) { oldpw ->
                required(word("senhaNova")) {newpw ->
                    launch {
                        val changed = Routes.Auth.ChangePassword(ChangePassword(player!!.uuid, player!!.ip, oldpw().value(), newpw().value())).handleErr { sendError(it, "verificando se sua conta existe") } ?: return@launch
                        if (!changed) {
                            sendFeedback {
                                buildText {
                                    mudarSenha {
                                        literal(" A sua senha não foi alterada. Verifique se ")
                                        color(Color.YELLOW) {
                                            literal("\"${oldpw().value()}\"")
                                        }
                                        literal(" foi digitado corretamente, e tente novamente.")
                                    }
                                }
                            }
                        } else {
                            sendFeedback {
                                buildText {
                                    mudarSenha {
                                        literal(" Senha alterada com sucesso.")
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
