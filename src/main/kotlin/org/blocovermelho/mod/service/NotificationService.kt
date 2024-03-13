package org.blocovermelho.mod.service

import com.mojang.authlib.GameProfile
import io.ktor.util.network.*
import net.minecraft.server.PlayerManager
import net.minecraft.server.network.ServerPlayerEntity
import org.blocovermelho.mod.api.ws.messages.LinkResult
import org.blocovermelho.mod.ext.Commands.ban
import org.blocovermelho.mod.ext.Commands.link
import org.blocovermelho.mod.ext.Helpers.hint
import org.blocovermelho.mod.ext.Helpers.tooltip
import org.blocovermelho.mod.ext.Other.serverHeader
import org.quiltmc.qkl.library.text.Color
import org.quiltmc.qkl.library.text.buildText
import org.quiltmc.qkl.library.text.color
import org.quiltmc.qkl.library.text.literal
import java.net.SocketAddress

object NotificationService {
    fun successfulLink(player: ServerPlayerEntity, result: LinkResult) {
        player.sendSystemMessage(buildText {
            link {
                literal(" Sua conta foi conectada com o discord.")
            }
        })
        player.sendSystemMessage(buildText {
            color(Color.GREEN) {
                literal(result.discordUsername)
            }
            color(Color.YELLOW) {
                literal(" => ")
            }
            color(Color.GREEN) {
                literal(player.gameProfile.name)
            }
        })
        player.sendSystemMessage(buildText {
            serverHeader {
                hint(" Caso essa não for sua conta", "contate a staff imediatamente.")
            }
        })
    }

    fun successfulIdentity(player: ServerPlayerEntity) {
        player.sendSystemMessage(buildText {
            serverHeader {
                color(Color.GREEN) {
                    literal(" Identidade verificada com sucesso.")
                }
            }
        })
    }

    fun failedIdentity(player: ServerPlayerEntity) {
        player.sendSystemMessage(buildText {
            serverHeader {
                color(Color.RED) {
                    literal(" Falha ao verificar sua identidade.")
                }
            }
        })
    }

    fun newBan(banReason: String, manager: PlayerManager, gameProfile: GameProfile, address: SocketAddress) {
        manager.broadcastSystemMessage(buildText {
            ban {
                color(Color.YELLOW) {
                    literal(" ${gameProfile.name}")
                }
                literal(" baniu ")
                tooltip({
                    literal("um gringo safado")
                }){
                    literal("Motivo: $banReason\n")
                    literal("IP: ${address.address}")
                }
            }
        }, false)
    }

    fun mergedBan(banReason: String, manager: PlayerManager, gameProfile: GameProfile, address: SocketAddress) {
        manager.broadcastSystemMessage(buildText {
            ban {
                color(Color.YELLOW) {
                    literal(" ${gameProfile.name}")
                }
                literal(" baniu ")
                tooltip({
                    literal("um gringo persistente")
                }){
                    literal("Motivo: $banReason\n")
                    literal("IP: ${address.address}")
                }
            }
        }, false)
    }
}
