package org.blocovermelho.bvauth.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import kotlinx.coroutines.launch
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import org.blocovermelho.bvauth.BvAuthMod
import org.blocovermelho.bvauth.api.routes.Discord
import org.blocovermelho.bvauth.ext.Core.maskedUri
import org.blocovermelho.bvauth.ext.Headers
import org.blocovermelho.bvauth.ext.dsl.buildLine
import org.blocovermelho.bvauth.ext.dsl.lineBreak
import org.blocovermelho.bvauth.ext.dsl.plusAssign
import org.blocovermelho.bvauth.ext.message
import org.blocovermelho.bvauth.ext.username
import org.blocovermelho.bvauth.impl.CoroutineManager
import org.blocovermelho.bvauth.impl.Err
import org.blocovermelho.bvauth.impl.Ok
import java.net.URI

object Link {
    fun register(
        dispatcher: CommandDispatcher<CommandSourceStack>,
    ) {
        val root = Commands.literal("link").requires { it.isPlayer }.executes {
            val player = it.source.player!!
            val profile = BvAuthMod.KnownProfiles[player.uuid]

            CoroutineManager.scope.launch {
                if (profile != null) {
                    player.sendSystemMessage(
                        buildLine {
                            this += buildLine( Headers.Server, Headers.Link)
                            this += "A conta já está linkada no perfil: ${profile.username} Discord=(${profile.discordId})"
                            lineBreak()
                            this += "Caso considere isto um engano ou queira mudar de conta do discord, entre em contato com a staff."
                        }
                    )
                } else {
                    val newLink = Discord.GetNewLink(player.gameProfile.name)
                    player.sendSystemMessage(
                        buildLine {
                            this += listOf(Headers.Server, Headers.Link)
                            this += "Link Automático"
                            this += { maskedUri(URI.create(newLink.replace("\"", "")), "Clique Aqui") }
                            this += "para abrir um navegador e linkar sua conta do discord."
                        })
                }
            }
            1
        }.then(Commands.argument("token", StringArgumentType.greedyString()).executes {
            val player = it.source.player!!
            val profile = BvAuthMod.KnownProfiles[player.uuid]
            val token = StringArgumentType.getString(it, "token").trim()

            if (profile != null) {
                player.sendSystemMessage(
                    buildLine {
                        this += listOf( Headers.Server, Headers.Link)
                        this += "A conta já está linkada no perfil: ${profile.username} Discord=(${profile.discordId})"
                        lineBreak()
                        this += "Caso considere isto um engano ou queira mudar de conta do discord, entre em contato com a staff."
                    })
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