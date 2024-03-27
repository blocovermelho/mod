package org.blocovermelho.mod.service

import com.mojang.authlib.GameProfile
import io.ktor.util.network.*
import net.minecraft.server.PlayerManager
import net.minecraft.server.network.ServerPlayerEntity
import org.blocovermelho.mod.api.ws.messages.LinkResult
import org.blocovermelho.mod.ext.Commands.ban
import org.blocovermelho.mod.ext.Commands.link
import org.blocovermelho.mod.ext.Helpers.tooltip
import org.blocovermelho.mod.ext.Other.serverHeader
import org.blocovermelho.mod.ext.Rich
import org.blocovermelho.mod.ext.Rich.lineOf
import org.blocovermelho.mod.ext.Rich.lines
import org.quiltmc.qkl.library.text.*
import java.net.SocketAddress

object NotificationService {
    fun successfulLink(player: ServerPlayerEntity, result: LinkResult) {
        player.sendSystemMessage(buildText {
            link {
                translatable("bv.link.success")
            }
        })
        player.sendSystemMessage(buildText {
            lineOf(
                { literal("Discord:") },
                {
                    lineOf({
                        literal(result.discordUsername)
                    }, {
                        color(Color.YELLOW) {
                            literal("<=>")
                        }
                    }, {
                        literal(player.gameProfile.name)
                    })

                }
            )
        })
        player.sendSystemMessage(buildText {
            serverHeader {
                color(Color.RED) {
                    translatable("bv.link.warning")
                }
            }
        })
    }

    fun successfulIdentity(player: ServerPlayerEntity) {
        player.sendSystemMessage(buildText {
            serverHeader {
                color(Color.GREEN) {
                    translatable("bv.identity.success")
                }
            }
        })
    }

    fun failedIdentity(player: ServerPlayerEntity) {
        player.sendSystemMessage(buildText {
            serverHeader {
                color(Color.RED) {
                    translatable("bv.identity.failed")
                }
            }
        })
    }

    fun TextBuilder.playerNotFound(target: ServerPlayerEntity) {
        serverHeader {
            translatable("bv.player.not_found", Rich.colorize(target.gameProfile.name, Color.YELLOW))
        }
    }

    fun TextBuilder.staffActor(staff: ServerPlayerEntity) {
        translatable("bv.actor.staff", Rich.colorize(staff.gameProfile.name, Color.GREY))
    }


    fun newBan(banReason: String, manager: PlayerManager, gameProfile: GameProfile, address: SocketAddress) {
        manager.broadcastSystemMessage(buildText {
            ban {
                translatable("bv.autoban.other", buildText {
                    color(Color.YELLOW) {
                        literal(gameProfile.name)
                    }
                }, buildText {
                    tooltip({
                        lines(
                            { translatable("bv.autoban.reason", buildText { literal(banReason) }) },
                            { literal("IP: ${address.address}") }
                        )
                    }) {
                        translatable("bv.autoban.moniker")
                    }
                })
            }
        }, false)
    }

    fun mergedBan(banReason: String, manager: PlayerManager, gameProfile: GameProfile, address: SocketAddress) {
        manager.broadcastSystemMessage(buildText {
            ban {
                translatable("bv.autoban.other", buildText {
                    color(Color.YELLOW) {
                        literal(gameProfile.name)
                    }
                }, buildText {
                    tooltip({
                        lines(
                            { translatable("bv.autoban.reason", buildText { literal(banReason) }) },
                            { literal("IP: ${address.address}") }
                        )
                    }) {
                        translatable("bv.autoban.moniker.persistent")
                    }
                })
            }
        }, false)
    }
}
