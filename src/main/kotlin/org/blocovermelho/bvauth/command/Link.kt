package org.blocovermelho.bvauth.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import kotlinx.coroutines.launch
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.server.level.ServerPlayer
import org.blocovermelho.bvauth.BvAuthMod
import org.blocovermelho.bvauth.api.routes.Discord
import org.blocovermelho.bvauth.api.routes.rProfile
import org.blocovermelho.bvauth.compat.BedrockGeyserCompat
import org.blocovermelho.bvauth.ext.Core.maskedUri
import org.blocovermelho.bvauth.ext.Headers
import org.blocovermelho.bvauth.ext.dsl.buildLine
import org.blocovermelho.bvauth.ext.dsl.lineBreak
import org.blocovermelho.bvauth.ext.dsl.plusAssign
import org.blocovermelho.bvauth.ext.message
import org.blocovermelho.bvauth.ext.username
import org.blocovermelho.bvauth.forms.ConnectBedrockForm
import org.blocovermelho.bvauth.forms.LinkForm
import org.blocovermelho.bvauth.forms.ProfileLinkOptions
import org.blocovermelho.bvauth.forms.SingleLinkOptions
import org.blocovermelho.bvauth.impl.CoroutineManager
import org.blocovermelho.bvauth.impl.Err
import org.blocovermelho.bvauth.impl.Ok
import org.blocovermelho.bvauth.impl.ok
import java.net.URI

object Link {
    fun register(
        dispatcher: CommandDispatcher<CommandSourceStack>,
    ) {
        val root = Commands.literal("link").requires { it.isPlayer }.executes {
            val player = it.source.player!!
            val server = it.source.server!!
            val profile = BvAuthMod.KnownProfiles[player.uuid]

            if (profile != null) {
                player.sendSystemMessage(
                    buildLine {
                        this += buildLine(Headers.Server, Headers.Link)
                        this += "A conta já está linkada no perfil: ${profile.username} Discord=(${profile.discordId})"
                        lineBreak()
                        this += "Caso considere isto um engano ou queira mudar de conta do discord, entre em contato com a staff."
                    }
                )
            }

            var _break = false
            BvAuthMod.BedrockCompat.ifPresent { compat ->
                _break = compat.handleBedrockPlayer(player)
            }

            if (_break) {
                return@executes 1
            }



            CoroutineManager.scope.launch {
                val newLink = Discord.GetNewLink(player.gameProfile.name)
                player.sendSystemMessage(
                    buildLine {
                        this += listOf(Headers.Server, Headers.Link)
                        this += "Link Automático"
                        this += { maskedUri(URI.create(newLink.replace("\"", "")), "Clique Aqui") }
                        this += "para abrir um navegador e linkar sua conta do discord."
                    })
            }
            1
        }.then(Commands.argument("token", StringArgumentType.greedyString()).executes {
            val player = it.source.player!!
            val profile = BvAuthMod.KnownProfiles[player.uuid]
            val token = StringArgumentType.getString(it, "token").trim()



            if (profile != null) {
                player.sendSystemMessage(
                    buildLine {
                        this += listOf(Headers.Server, Headers.Link)
                        this += "A conta já está linkada no perfil: ${profile.username} Discord=(${profile.discordId})"
                        lineBreak()
                        this += "Caso considere isto um engano ou queira mudar de conta do discord, entre em contato com a staff."
                    })
                return@executes 1
            }

            var _break = false
            BvAuthMod.BedrockCompat.ifPresent { compat ->
                _break = compat.handleBedrockPlayer(player)
            }

            if (_break) {
                return@executes 1
            }

            CoroutineManager.scope.launch {
                val link = Discord.ManualLink(player.gameProfile.name, token)
                when (link) {
                    is Ok -> {
                        if (link.value.isMember) {
                            BvAuthMod.DiscordLinks[player.username()] = link.value
                        }
                    }

                    is Err -> {
                        player.sendSystemMessage(
                            buildLine(
                                Headers.Server,
                                Headers.Register,
                                link.message("a obtenção da sua conta do discord.", "Tente novamente com outro token."),
                            )
                        )
                    }
                }

            }

            1
        })

        dispatcher.register(root)
    }
}

suspend fun connectProfile(profile: String, player: ServerPlayer) {
    rProfile.ConnectBedrock(profile, player.username())
    player.connection.disconnect(buildLine {
        this += buildLine(Headers.Server, Headers.Link)
        this += "Reconecte ao servidor para logar com a conta recém-conectada"
    })
}

fun BedrockGeyserCompat.handleBedrockPlayer(player: ServerPlayer) : Boolean{
    var isBedrock = false
    this.getConnectionByUUID(player.uuid).ifPresent { conn ->
        isBedrock = true

        this.sendFormToUser(LinkForm { token ->
            val link = Discord.ManualLink(player.username(), token).ok()
            if (link != null) {
                val profiles = rProfile.ResolveDiscord(link.discordId).ok().orEmpty()
                if (!profiles.isEmpty()) {
                    if (profiles.size == 1) {
                        val profile = profiles[0]
                        this.sendFormToUser(SingleLinkOptions(profile, link, {
                            BvAuthMod.Logger.info("Bedrock player made new profile.")
                            BvAuthMod.DiscordLinks[player.username()] = link
                        }, {
                            BvAuthMod.Logger.info("Bedrock player linked to existing profile.")
                            connectProfile(profile.username, player)
                        }), conn);
                    } else {
                        this.sendFormToUser(
                            ProfileLinkOptions(
                                profiles.size, link,
                                {
                                    BvAuthMod.Logger.info("Bedrock player made new profile.")
                                    BvAuthMod.DiscordLinks[player.username()] = link
                                }, {
                                    BvAuthMod.Logger.info("Bedrock player linked to existing profile.")
                                    this.sendFormToUser(ConnectBedrockForm(profiles, player.username()) { profile ->
                                        connectProfile(profile, player)
                                    }, conn)
                                }), conn
                        )
                    }
                }
            }
        }, conn)

    }

    return isBedrock
}