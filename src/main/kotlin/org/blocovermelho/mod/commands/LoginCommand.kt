package org.blocovermelho.mod.commands

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.server.command.ServerCommandSource
import org.blocovermelho.mod.BVQuilt
import org.blocovermelho.mod.BVQuilt.Store.ServerUUID
import org.blocovermelho.mod.api.Routes
import org.blocovermelho.mod.ext.*
import org.blocovermelho.mod.api.handleErr
import org.blocovermelho.mod.api.models.FirstJoin
import org.blocovermelho.mod.api.models.LoginRequest
import org.blocovermelho.mod.api.models.Resume
import org.blocovermelho.mod.ext.launch
import org.blocovermelho.mod.ext.Commands.login
import org.blocovermelho.mod.ext.Helpers.command
import org.blocovermelho.mod.ext.Other.welcomeMessage
import org.blocovermelho.mod.ext.Rich.lineOf
import org.blocovermelho.mod.ext.Rich.lines
import org.quiltmc.qkl.library.brigadier.argument.value
import org.quiltmc.qkl.library.brigadier.argument.word
import org.quiltmc.qkl.library.brigadier.register
import org.quiltmc.qkl.library.brigadier.required
import org.quiltmc.qkl.library.brigadier.util.broadcastSystemMessage
import org.quiltmc.qkl.library.brigadier.util.sendFeedback
import org.quiltmc.qkl.library.brigadier.util.server
import org.quiltmc.qkl.library.text.*

object LoginCommand {
     fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register("login") {
            requires { it.isPlayer && !it.player!!.isLogged() && it.player!!.isKnown() }
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
                                    lines(
                                        { translatable("bv.player.self.not_registered") },
                                        {
                                            lineOf(
                                                { translatable("bv.action.use", buildText { command("/registrar") }) },
                                                { translatable("bv.register.hint") }
                                            )
                                        }
                                    )
                                }
                            }
                        }
                        return@launch
                    }

                    val correct = Routes.Auth.Login(ServerUUID, LoginRequest(user.uuid, user.ip, pwd)).handleErr {
                        sendError(it, "verificação do seu login")
                    }

                    if (correct == null) {
                        sendFeedback { buildText { login { translatable("bv.login.failed") } } }
                        return@launch
                    }

                    when (correct) {
                        is FirstJoin -> {
                            broadcastSystemMessage(buildText { welcomeMessage(player) }, false)
                        }
                        is Resume -> {
                            val viewport = correct.viewport
                            val worldRegKey = server.worldRegistryKeys.first { it.value.toString() == viewport.loc.dim }
                            val world = server.getWorld(worldRegKey)
                            player.teleport(world, viewport.loc.x, viewport.loc.y, viewport.loc.z,
                                viewport.yaw.toFloat(), viewport.pitch.toFloat())
                        }
                    }

                    BVQuilt.Store.LoggedPlayers.add(player.uuid)
                    player.changeGameMode(BVQuilt.SERVER_DATA.postLoginGamemode.value())

                    player.updateCommandTree()
                    sendFeedback { buildText { login { translatable("bv.login.success")  } } }
                }
            }
        }
    }

}
