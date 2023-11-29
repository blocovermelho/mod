package org.blocovermelho.mod.events

import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.GameMode
import org.blocovermelho.mod.BVQuilt
import org.blocovermelho.mod.api.Routes
import org.blocovermelho.mod.api.handleErr
import org.blocovermelho.mod.ext.Commands.login
import org.blocovermelho.mod.ext.Helpers.command
import org.blocovermelho.mod.ext.Helpers.maskedUri
import org.blocovermelho.mod.ext.Other.serverHeader
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
        Routes.Auth.Resume(player.uuid).handleErr { player.sendErr(it, "atualizando dados da sua conta") }
        BVQuilt.Store.LoggedPlayers.add(player.uuid)
        player.sendSystemMessage(buildText {
            login {
                literal("Sessão restaurada. Seja bem vinde de volta ")
                color(Color.YELLOW) {
                    literal(player.gameProfile.name)
                }
            }
        })

        player.changeGameMode(BVQuilt.SERVER_DATA.postLoginGamemode.value())
        player.updateCommandTree()

    } else {
        player.sendSystemMessage(buildText {
            login {
                literal(" Sessão expirada. Use")
                command("/login")
                literal(" senha para logar no servidor.")
            }
        })
    }

    return
}


fun sendServerDetails(player: ServerPlayerEntity) {
    player.sendSystemMessage(buildText {
        serverHeader {
            literal(" Olá ")
            color(Color.YELLOW){
                literal(player.gameProfile.name)
            }
            literal(" seja bem-vinde ao ")
            color(Color.DARK_GREEN) {
                literal(BVQuilt.SERVER_DATA.name.value())
            }
            literal("!\nVersões aceitas nesse servidor: ")
            color(Color.RED) {
                BVQuilt.SERVER_DATA.supportedVersions.value().forEach {
                    literal("$it ")
                }
            }
            if(BVQuilt.SERVER_DATA.modpack.name.value().isEmpty().not()) {
                literal("\nModpack recomendado: ")
                maskedUri(BVQuilt.SERVER_DATA.modpack.uri.value(),BVQuilt.SERVER_DATA.modpack.name.value())
            }
        }
    })
}
suspend fun sendAwkMessage(player: ServerPlayerEntity) {
    val acc = Routes.User.Exists(player.uuid).handleErr { player.sendErr(it, "verificando se sua conta existe") } ?: return

    if (!acc) {
        player.sendSystemMessage(buildText {
            serverHeader {
                literal(" Vi aqui que essa provavelmente é a primeira vez que joga no servidor.\n")
                literal(" Para jogar conosco é ")
                color(Color.RED) {
                    bold {
                        literal("obrigatório")
                    }
                }
                literal(" que você esteja no nosso discord.\n")
                literal(" Peça a quem tenha te convidado o link para o nosso discord, ou simplesmente ignore essa mensagem e explore o mundo!\n")
            }

            serverHeader {
                literal(" Se você já estiver no nosso discord, use o comando")
                command("/link")
                literal(" para conectar sua conta do discord.")
            }
        })
    }
}
