package org.blocovermelho.mod.commands

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.server.command.ServerCommandSource
import org.blocovermelho.mod.BVQuilt
import org.blocovermelho.mod.ext.Colors
import org.blocovermelho.mod.ext.Commands.ban
import org.blocovermelho.mod.ext.isBypassing
import org.blocovermelho.mod.ext.isLogged
import org.quiltmc.qkl.library.brigadier.argument.*
import org.quiltmc.qkl.library.brigadier.execute
import org.quiltmc.qkl.library.brigadier.register
import org.quiltmc.qkl.library.brigadier.required
import org.quiltmc.qkl.library.brigadier.util.sendFeedback
import org.quiltmc.qkl.library.text.Color
import org.quiltmc.qkl.library.text.buildText
import org.quiltmc.qkl.library.text.color
import org.quiltmc.qkl.library.text.literal

object GringoCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register("gringo") {
            requires { it.isPlayer && it.player!!.isLogged() && !it.player!!.isBypassing() }

            required(literal("add")) {
                required(greedyString("mensagem")) { msg ->
                    execute {
                        val valor = msg().value()
                        BVQuilt.Store.BanReasons.add(valor)
                        BVQuilt.Store.flush()
                        sendFeedback {
                            buildText {
                                ban {
                                    literal("Adicionado ")
                                    color(Color.YELLOW) {
                                        literal("\"$valor\"")
                                    }
                                    literal(" à lista de mensagens possiveis.")
                                }
                            }
                        }
                    }
                }
            }

            required(literal("list")) {
                execute {
                    val seven = BVQuilt.Store.BanReasons.shuffled().take(7).joinToString(separator = "\n")
                    sendFeedback {
                        buildText {
                            ban {
                                color(Colors.AUTH) {
                                    literal(" Essa é uma lista com 7 motivos de banimento aleatórios enviados pelos camaradas do Bloco Vermelho\n")
                                }
                                color(Color.GREY) {
                                    literal("A sua mensagem *provavelmente* não vai estar aqui. Ela vai aparecer pra algum gringo desavisado eventualmente.\n")
                                }
                                literal(seven)
                            }
                        }
                    }
                }
            }
        }
    }
}

