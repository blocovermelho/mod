package org.blocovermelho.mod.commands.admin

import org.blocovermelho.mod.BVQuilt
import org.blocovermelho.mod.api.Routes
import org.blocovermelho.mod.api.handleErr
import org.blocovermelho.mod.ext.Helpers.command
import org.blocovermelho.mod.ext.Other.serverHeader
import org.blocovermelho.mod.ext.Rich.colorize
import org.blocovermelho.mod.ext.Rich.lineOf
import org.blocovermelho.mod.ext.Rich.lines
import org.blocovermelho.mod.ext.Subcommand
import org.blocovermelho.mod.ext.launch
import org.blocovermelho.mod.ext.sendErr
import org.quiltmc.qkl.library.brigadier.argument.literal
import org.quiltmc.qkl.library.brigadier.argument.player
import org.quiltmc.qkl.library.brigadier.argument.value
import org.quiltmc.qkl.library.brigadier.required
import org.quiltmc.qkl.library.brigadier.util.player
import org.quiltmc.qkl.library.brigadier.util.sendFeedback
import org.quiltmc.qkl.library.text.Color
import org.quiltmc.qkl.library.text.buildText
import org.quiltmc.qkl.library.text.translatable

fun Subcommand.unlock() {
    required(literal("unlock")) {
        required(player("user")) {usr ->
            launch {
                val user = usr().value()
                val uuid = user.uuid
                val remove = BVQuilt.Store.BypassCidrCheck.remove(uuid)
                if (remove != null) {
                    // Try Using the nonce to allow the current ip.
                    if (remove.isNotEmpty()) {
                        val succ = Routes.Auth.CIDR.Allow(uuid, user.ip, remove).handleErr { player!!.sendErr(it, "Tentando adicionar o IP manualmente") } ?: false

                        if (succ) {
                            // Could add manually the current IP to allowed IPs
                            sendFeedback {
                                buildText {
                                    serverHeader {
                                        translatable("bv.unlock.other.success", colorize(user.gameProfile.name, Color.YELLOW))
                                    }
                                }
                            }

                            user.sendSystemMessage(
                                buildText {
                                    serverHeader {
                                        lines(
                                            { translatable("bv.unlock.self.success") },
                                            { translatable("bv.actor.staff", colorize(player!!.gameProfile.name, Color.YELLOW))},
                                            { lineOf(
                                                { translatable("bv.action.use", buildText { command("/login")  }) },
                                                { translatable("bv.login.hint") }
                                            )}
                                        )
                                    }
                                }
                            )
                        } else {
                            // Failed again, but the user was removed from the bypass cidr list.
                            sendFeedback {
                                buildText {
                                    translatable("bv.unlock.other.failed", colorize(user.gameProfile.name, Color.YELLOW))
                                }
                            }

                            user.sendSystemMessage(
                                buildText {
                                    serverHeader {
                                        lines(
                                            { translatable("bv.unlock.self.failed") },
                                            { translatable("bv.actor.staff", colorize(player!!.gameProfile.name, Color.YELLOW))}
                                        )
                                    }
                                }
                            )
                        }
                    }
                } else {
                    sendFeedback {
                        buildText {
                            serverHeader {
                                translatable("bv.unlock.other.not_found", colorize(user.gameProfile.name, Color.YELLOW))
                            }
                        }
                    }
                }
            }
        }
    }
}
