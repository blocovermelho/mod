package org.blocovermelho.bvauth


import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import org.blocovermelho.bvauth.actor.KeepAliveActor.Companion.spawnKeepAlive
import org.blocovermelho.bvauth.actor.PlayerNotificationActor.Companion.spawnPlayerNotification
import org.blocovermelho.bvauth.actor.PlayerNotificationActorHandle
import org.blocovermelho.bvauth.actor.WebsocketActor.Companion.spawnWebsocket
import org.blocovermelho.bvauth.api.routes.GameServer
import org.blocovermelho.bvauth.api.routes.rProfile
import org.blocovermelho.bvauth.api.types.Login
import org.blocovermelho.bvauth.api.types.Profile
import org.blocovermelho.bvauth.api.types.WebSocketMessage
import org.blocovermelho.bvauth.command.ChangeId
import org.blocovermelho.bvauth.command.ChangePassword
import org.blocovermelho.bvauth.command.Link
import org.blocovermelho.bvauth.command.Register
import org.blocovermelho.bvauth.command.cLogin
import org.blocovermelho.bvauth.config.ModConfig
import org.blocovermelho.bvauth.event.IdentityResolveEvent
import org.blocovermelho.bvauth.event.PreLoginEvent
import org.blocovermelho.bvauth.ext.Colors
import org.blocovermelho.bvauth.ext.Headers
import org.blocovermelho.bvauth.ext.STFBuilder.asComponent
import org.blocovermelho.bvauth.ext.STFBuilder.color
import org.blocovermelho.bvauth.ext.STFBuilder.italic
import org.blocovermelho.bvauth.ext.STFBuilder.player
import org.blocovermelho.bvauth.ext.STFBuilder.suggestCommand
import org.blocovermelho.bvauth.ext.getIpString
import org.blocovermelho.bvauth.ext.username
import org.blocovermelho.bvauth.impl.ApiClient
import org.blocovermelho.bvauth.impl.ApiClient.settings
import org.blocovermelho.bvauth.impl.CoroutineManager
import org.blocovermelho.bvauth.impl.expect
import org.blocovermelho.bvauth.impl.ok
import org.slf4j.LoggerFactory
import java.util.*

class BvAuthMod : ModInitializer {

    override fun onInitialize() {

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            Link.register(dispatcher)
            cLogin.register(dispatcher)
            Register.register(dispatcher)
            ChangePassword.register(dispatcher)
        }

        ServerLifecycleEvents.SERVER_STARTED.register {
            PlayerNotification = CoroutineManager.scope.spawnPlayerNotification(it.playerList)
            runBlocking {
                val me = GameServer.Me().expect { "A gameserver must exist for the server to use this mod." }
                Config.Server.Nome = me.name
                Config.Server.Versoes = me.versions
                Config.Server.Staff = me.staff.map { it.username }

                Config.save()

                Logger.info("Carregado informações sobre o servidor ${me.name} para a config.")
            }
        }

        ServerPlayConnectionEvents.JOIN.register { impl, sender, server ->
            val player = impl.player

            runBlocking {
                val restore = rProfile.SessionRestore(player.username())
                if (restore) {
                    LoggedUsers += player.uuid
                    player.setGameMode(Config.Gamemode)
                    player.sendSystemMessage(
                        listOf(Headers.Server,
                            "Sessão restaurada.".color(Colors.COMMAND_GREEN)
                        ).joinToString(" ").asComponent())
                } else {
                    val p = KnownProfiles[player.uuid]
                    if (p != null) {
                        player.sendSystemMessage(
                            listOf(Headers.Server,
                                "Bem vinde de volta ", p.username.color(Colors.COMMAND_GREEN),
                                "! use ", "/login".suggestCommand("/login"), "para logar no servidor."
                            ).joinToString(" ").asComponent())
                    } else {
                        player.sendSystemMessage(
                            listOf(Headers.Server,
                                "Seja bem-vinde ao servidor" , player.username().color(Colors.COMMAND_GREEN),
                                "! Para jogar no servidor use o comando", "/link".suggestCommand("/link"), "para linkar sua conta do discord e começe o processo de criação do seu perfil."
                            ).joinToString(" ").asComponent())
                    }
                }
            }

            KeepAlive.playerJoined(player.username())
        }

        ServerPlayConnectionEvents.DISCONNECT.register { impl, server ->
            LoggedUsers -= impl.player.uuid
            KnownProfiles -= impl.player.uuid
            runBlocking {
                rProfile.Logout(impl.player.username())
            }
            KeepAlive.playerLeft(impl.player.username())
        }

        IdentityResolveEvent.IDENTITY_RESOLVE.register { address, username ->
            runBlocking {
                val prof = rProfile.Get(username).ok()

                if (prof != null && !KnownProfiles.contains(prof.id)) {
                    KnownProfiles.putIfAbsent(prof.id, prof)
                }

                prof?.id
            }
        }

        PreLoginEvent.PRE_LOGIN.register { server, address, id ->
            val profile = KnownProfiles[id.id]
            if (profile != null) {
                return@register runBlocking {
                    val login = rProfile.Login(profile.username, address.getIpString()).expect { "Profile exists." }
                    return@runBlocking when (login) {
                        Login.AllowedIp -> {
                            null
                        }

                        Login.BannedIp -> {
                            listOf(Headers.Server,
                                "Seu IP foi banido do servidor.\n".color(Colors.ERR),
                                "Your IP was banned from the server.\n\n".color(ChatFormatting.GRAY).italic(),

                                "IP:", address.getIpString().color(Colors.COMMAND_GREEN)
                            ).joinToString (" ").asComponent()
                        }

                        Login.BlockedIp -> {
                            listOf(Headers.Server,
                                "Seu IP foi recentemente blockeado pelo servidor.\n".color(Colors.ERR),
                                "Your IP was recently blocked by the server.\n\n".color(ChatFormatting.GRAY).italic(),

                                "IP:", address.getIpString().color(Colors.COMMAND_GREEN)
                            ).joinToString (" ").asComponent()
                        }

                        Login.NewIp -> {
                            listOf(Headers.Server,
                                "Novo IP Detectado.".color(Colors.INFO), "Permita".color(Colors.COMMAND_GREEN), "ou", "Recuse".color(Colors.READ_ONLY_RED) ,"na sua conta do discord.\n".color(Colors.INFO),
                                "New IP Detected. Allow/Deny on your linked discord account.\n\n".color(ChatFormatting.GRAY).italic(),

                                "IP:", address.getIpString().color(Colors.COMMAND_GREEN)
                            ).joinToString (" ").asComponent()
                        }
                    }
                }
            }

            null
        }
    }

    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        val Json = Json {
            classDiscriminator = "kind"
            namingStrategy = JsonNamingStrategy.SnakeCase
            explicitNulls = false
        }

        val Logger = LoggerFactory.getLogger("BVMod")
        val Config =
            ModConfig.createToml<ModConfig>(FabricLoader.getInstance().configDir, "", "bvauth", ModConfig::class.java)
        val Api = ApiClient.init(Config.Auth)
        val KeepAlive = CoroutineManager.scope.spawnKeepAlive()
        var PlayerNotification : PlayerNotificationActorHandle? = null
        val Websocket = CoroutineManager.scope.spawnWebsocket(
            if (settings.TLS) {
                "wss://"
            } else {
                "ws://"
            } + Config.Auth.Endpoint + "/server/@me/ws", Api.client, Config.Auth.ApiToken
        )
        var KnownProfiles = mutableMapOf<UUID, Profile>()
        var DiscordLinks = mutableMapOf<String, WebSocketMessage.DiscordLink>()
        var LoggedUsers = mutableSetOf<UUID>()

    }
}
