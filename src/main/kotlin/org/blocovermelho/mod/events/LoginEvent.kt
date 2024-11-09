package org.blocovermelho.mod.events

import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.GameMode
import org.blocovermelho.mod.BVQuilt
import org.blocovermelho.mod.api.Routes
import org.blocovermelho.mod.api.handleErr
import org.blocovermelho.mod.ext.Commands.login
import org.blocovermelho.mod.ext.Helpers.bracketed
import org.blocovermelho.mod.ext.Helpers.command
import org.blocovermelho.mod.ext.Helpers.maskedUri
import org.blocovermelho.mod.ext.Other.serverHeader
import org.blocovermelho.mod.ext.Rich.array
import org.blocovermelho.mod.ext.Rich.colorize
import org.blocovermelho.mod.ext.Rich.lineOf
import org.blocovermelho.mod.ext.Rich.lines
import org.blocovermelho.mod.ext.sendErr
import org.blocovermelho.mod.ext.updateCommandTree
import org.quiltmc.qkl.library.text.*

suspend fun checkSession(player: ServerPlayerEntity)  {
    val acc = Routes.Auth.Exists(player.uuid).handleErr { player.sendErr(it, "verificando se sua conta existe") } ?: return

    if (!acc) {
        return
    }

    val session = Routes.Auth.Session(player.uuid, player.ip).handleErr { player.sendErr(it, "verificando se possui uma sessão ativa") } ?: return

    if (session) {
        Routes.Auth.Resume(player.uuid, player.ip).handleErr { player.sendErr(it, "atualizando dados da sua conta") }
        BVQuilt.Store.LoggedPlayers.add(player.uuid)
        player.sendSystemMessage(buildText {
            login {
                translatable("bv.session.restored", colorize(player.gameProfile.name, Color.YELLOW))
            }
        })

        player.changeGameMode(BVQuilt.SERVER_DATA.postLoginGamemode.value())
        player.updateCommandTree()

    } else {
        player.sendSystemMessage(buildText {
            login {
                lines(
                    { translatable("bv.session.expired") },
                    { lineOf(
                        { translatable("bv.action.use", buildText { command("/login")  }) },
                        { translatable("bv.login.hint") }
                    )}
                )
            }
        })
    }

    return
}


fun sendServerDetails(player: ServerPlayerEntity) {
    player.sendSystemMessage(buildText {
        serverHeader {
            lines(
                { translatable("bv.welcome.greet", colorize(player.gameProfile.name, Color.YELLOW), colorize(BVQuilt.SERVER_DATA.name.value(), Color.DARK_GREEN)) },
                { translatable("bv.welcome.versions", buildText {
                    array(BVQuilt.SERVER_DATA.supportedVersions.value()) {
                        color(Color.YELLOW) {
                            literal(it)
                        }
                    }
                })}
            )

            if (BVQuilt.SERVER_DATA.modpack.name.value().isNotEmpty()) {
                literal("\n")
                translatable("bv.welcome.modpack", buildText {
                    lineOf(
                        { maskedUri(BVQuilt.SERVER_DATA.modpack.uri.value(),BVQuilt.SERVER_DATA.modpack.name.value()) },
                        { bracketed(open = "(v.", close = ")"){ literal(BVQuilt.SERVER_DATA.modpack.version.value()) } }
                    )
                })
            }
        }
    })
}
suspend fun sendAwkMessage(player: ServerPlayerEntity) {
    val acc = Routes.User.Exists(player.uuid).handleErr { player.sendErr(it, "verificando se sua conta existe") } ?: return

    if (!acc) {
        player.sendSystemMessage(buildText {
            serverHeader {
                lines(
                    { translatable("bv.welcome.first_join[0]") },
                    { lineOf(
                        {translatable("bv.welcome.first_join[1]") },
                        {color(Color.RED) { bold { translatable("bv.required") }}},
                        {translatable("bv.welcome.first_join[2]")}
                    )},
                    { translatable("bv.welcome.first_join[3]")}
                )
            }

            literal("\n")

            serverHeader {
                translatable("bv.welcome.first_join[4]", buildText { command("/link") })
            }
        })
    }
}
