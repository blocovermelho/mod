package org.blocovermelho.bvauth.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import kotlinx.coroutines.launch
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import org.blocovermelho.bvauth.BvAuthMod
import org.blocovermelho.bvauth.api.routes.Discord
import org.blocovermelho.bvauth.ext.Colors
import org.blocovermelho.bvauth.ext.Headers
import org.blocovermelho.bvauth.ext.STFBuilder.asComponent
import org.blocovermelho.bvauth.ext.STFBuilder.color
import org.blocovermelho.bvauth.ext.STFBuilder.maskedUrl
import org.blocovermelho.bvauth.ext.STFBuilder.showText
import org.blocovermelho.bvauth.ext.STFBuilder.suggestCommand
import org.blocovermelho.bvauth.ext.message
import org.blocovermelho.bvauth.ext.username
import org.blocovermelho.bvauth.impl.CoroutineManager
import org.blocovermelho.bvauth.impl.Err
import org.blocovermelho.bvauth.impl.Ok

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
                        listOf(
                            listOf(
                                Headers.Server, Headers.Link,
                                "A conta já está linkada no perfil: ${profile.username} Discord=(${profile.discordId})").joinToString(" "),
                            "Caso considere isto um engano ou queira mudar de conta do discord, entre em contato com a staff."
                        ).joinToString ( "\n" ).asComponent()
                    )
                } else {
                    val newLink = Discord.GetNewLink(player.gameProfile.name)
                    player.sendSystemMessage(
                        listOf(
                            Headers.Server, Headers.Link,
                            "Link Automático",
                            newLink.maskedUrl("Clique Aqui"),
                            "para abrir um navegador e linkar sua conta do discord."
                        ).joinToString(  " " ).asComponent())
                }
            }
            1
        }.then(Commands.argument("token", StringArgumentType.greedyString()).executes {
            val player = it.source.player!!
            val profile = BvAuthMod.KnownProfiles[player.uuid]
            val token = StringArgumentType.getString(it, "token").trim()

            if (profile != null) {
                player.sendSystemMessage(
                    listOf(
                        listOf(
                            Headers.Server, Headers.Link ,
                            "A conta já está linkada no perfil: ${profile.username} Discord=(${profile.discordId})").joinToString(" "),
                        "Caso considere isto um engano ou queira mudar de conta do discord, entre em contato com a staff."
                    ).joinToString ("\n" ).asComponent()
                )
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
                           listOf(
                               Headers.Server,
                               Headers.Register,
                               link.message("a obtenção da sua conta do discord.", "Tente novamente com outro token."),
                           ).joinToString(" ").asComponent()
                       )
                   }
                }

            }

            1
        })

        dispatcher.register(root)
    }
}