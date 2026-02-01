package org.blocovermelho.bvauth.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import kotlinx.coroutines.launch
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import org.blocovermelho.bvauth.BvAuthMod
import org.blocovermelho.bvauth.api.routes.rProfile
import org.blocovermelho.bvauth.api.types.CreateProfile
import org.blocovermelho.bvauth.api.types.NewProfile
import org.blocovermelho.bvauth.ext.Core.toLiteral
import org.blocovermelho.bvauth.ext.Headers
import org.blocovermelho.bvauth.ext.dsl.buildLine
import org.blocovermelho.bvauth.ext.message
import org.blocovermelho.bvauth.impl.*
import org.blocovermelho.bvauth.ext.unaryMinus

object Register {
    fun register(
        dispatcher: CommandDispatcher<CommandSourceStack>,
    ) {
        val root = Commands.literal("registrar").requires { it.isPlayer && BvAuthMod.DiscordLinks.containsKey(it.player?.gameProfile?.name) }
            .then(Commands.argument("senha", StringArgumentType.word())
                .then(Commands.argument("confirma", StringArgumentType.word())
                    .executes {

                    val senha = StringArgumentType.getString(it, "senha")
                    val confirma = StringArgumentType.getString(it, "confirma")

                    if (senha != confirma) {
                        it.source.sendFailure(
                            buildLine(
                                Headers.Server, Headers.Register, "As senhas informadas não são idênticas.".toLiteral()
                            )
                        )
                        return@executes 1
                    }

                    val player = it.source.player!!
                    val username = player.gameProfile!!.name
                    val server = it.source.server

                    CoroutineManager.scope.launch {
                        val profile = rProfile.Create(
                            username,
                            NewProfile(senha, BvAuthMod.DiscordLinks[username]!!.discordId)
                        )

                        when (profile) {
                            is Ok -> {
                                when (profile.value) {
                                    is CreateProfile.Created -> {
                                        val oldId = player.uuid

                                        (server.playerList as IdSwapper).`bv$swapId`(player.uuid, profile.value.id)
                                        (server.playerList as VisitorGetter).`bv$unsetVisitor`(oldId)

                                        player.sendSystemMessage(
                                            buildLine(
                                                Headers.Server, Headers.Register, "Perfil criado com sucesso.".toLiteral()
                                            )
                                        )

                                        val prof = rProfile.Get(username).expect { "Profile was created." }

                                        BvAuthMod.KnownProfiles[player.uuid] = prof
                                        player.setGameMode(-BvAuthMod.Config.Gamemode)
                                        server.commands.sendCommands(player)
                                    }

                                    CreateProfile.UsernameExists -> {
                                        player.sendSystemMessage(
                                            buildLine(
                                                Headers.Server,
                                                Headers.Register,
                                                "Seu perfil não pode ser criado pois o username já está em uso.".toLiteral()
                                            )
                                        )
                                    }
                                }
                            }

                            is Err -> {
                                player.sendSystemMessage(
                                    buildLine(
                                        Headers.Server,
                                        Headers.Register,
                                        profile.message("criação do seu perfil.", "Tente novamente."),
                                    )
                                )
                            }
                        }
                    }

                    1
                })
               )

        dispatcher.register(root)
    }
}