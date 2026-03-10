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
import org.blocovermelho.bvauth.actor.KeepAliveActor.Companion.spawnKeepAlive
import org.blocovermelho.bvauth.actor.PlayerNotificationActor.Companion.spawnPlayerNotification
import org.blocovermelho.bvauth.actor.PlayerNotificationActorHandle
import org.blocovermelho.bvauth.actor.WebsocketActor.Companion.spawnWebsocket
import org.blocovermelho.bvauth.api.routes.GameServer
import org.blocovermelho.bvauth.api.routes.Root
import org.blocovermelho.bvauth.api.routes.rProfile
import org.blocovermelho.bvauth.api.types.BedrockAccountStanding
import org.blocovermelho.bvauth.api.types.Login
import org.blocovermelho.bvauth.api.types.Profile
import org.blocovermelho.bvauth.api.types.WebSocketMessage
import org.blocovermelho.bvauth.command.ChangePassword
import org.blocovermelho.bvauth.command.Link
import org.blocovermelho.bvauth.command.Register
import org.blocovermelho.bvauth.command.cLogin
import org.blocovermelho.bvauth.compat.BedrockGeyserCompat
import org.blocovermelho.bvauth.compat.PlaceholderApiCompat
import org.blocovermelho.bvauth.config.ModConfig
import org.blocovermelho.bvauth.event.IdentityResolveEvent
import org.blocovermelho.bvauth.event.PreLoginEvent
import org.blocovermelho.bvauth.ext.Colors
import org.blocovermelho.bvauth.ext.Core.colorize
import org.blocovermelho.bvauth.ext.Core.suggestCmd
import org.blocovermelho.bvauth.ext.Core.toLiteral
import org.blocovermelho.bvauth.ext.Headers
import org.blocovermelho.bvauth.ext.dsl.Color
import org.blocovermelho.bvauth.ext.dsl.buildLine
import org.blocovermelho.bvauth.ext.dsl.color
import org.blocovermelho.bvauth.ext.dsl.italic
import org.blocovermelho.bvauth.ext.dsl.lineBreak
import org.blocovermelho.bvauth.ext.dsl.literal
import org.blocovermelho.bvauth.ext.dsl.plusAssign
import org.blocovermelho.bvauth.ext.dsl.text
import org.blocovermelho.bvauth.ext.getIpString
import org.blocovermelho.bvauth.ext.username
import org.blocovermelho.bvauth.impl.ApiClient
import org.blocovermelho.bvauth.impl.ApiClient.settings
import org.blocovermelho.bvauth.impl.CoroutineManager
import org.blocovermelho.bvauth.impl.expect
import org.blocovermelho.bvauth.impl.ok
import org.blocovermelho.bvauth.ext.divAssign
import org.blocovermelho.bvauth.ext.unaryMinus
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
                Config.Server.Nome /= me.name
                Config.Server.Versoes /= me.versions
                Config.Server.Staff /= me.staff.map { o -> o.username }

                Config.save()

                Logger.info("Carregado informações sobre o servidor ${me.name} para a config.")

                val onConfig = (-Config.Server.Versoes).toHashSet()
                val javaVersions =  onConfig.dropWhile { x -> x.startsWith("Bedrock") }.toSet()
                val toUpdate = javaVersions.toMutableSet()


                BedrockCompat.ifPresentOrElse({ bc ->
                    Logger.info("Versões atualmentes suportadas pelo Geyser-Fabric: ${bc.supportedBedrockVersions}")

                    PlaceholderCompat.ifPresent { ph ->
                        ph.registerBedrockPlaceholder(bc)
                    }

                    runBlocking {
                        val bedrockVersions = Root.GetVersionRanges(bc.supportedBedrockVersions).ok().orEmpty().map { version -> "Bedrock $version" }.toSet()
                        Logger.info("Bedrock versions: $bedrockVersions")
                        toUpdate += bedrockVersions
                    }
                }) {
                    PlaceholderCompat.ifPresent { ph ->
                        ph.registerDummyPlaceholders()
                    }
                }

                if (toUpdate != onConfig) {
                    Logger.info("Versões suportadas são diferentes. Atualizando: $onConfig -> $toUpdate.")
                    GameServer.UpdateVersions(toUpdate.toList())

                    Config.Server.Versoes /= toUpdate.toList()
                    Config.save()
                }
            }
        }

        ServerPlayConnectionEvents.JOIN.register { impl, sender, server ->
            val player = impl.player

            runBlocking {
                val profile = KnownProfiles[player.uuid]

                if (profile != null) {
                    val restore = rProfile.SessionRestore(profile.username)
                    if (restore) {
                        LoggedUsers += player.uuid
                        player.setGameMode(-Config.Gamemode)
                        player.sendSystemMessage(
                            buildLine(Headers.Server,
                                "Sessão restaurada.".colorize(Colors.COMMAND_GREEN)
                            ))
                    } else {
                        player.sendSystemMessage(
                            buildLine {
                                this += Headers.Server
                                this += "Bem vinde de volta"
                                this += profile.username.colorize(Colors.COMMAND_GREEN)
                                this += "! use"
                                this += { suggestCmd("/login") { text("/login".colorize(Colors.COMMAND_GREEN))} }
                                this += "para logar no servidor."
                            })
                    }
                } else {
                    player.sendSystemMessage(
                        buildLine {
                            this += Headers.Server
                            this += "Seja bem-vinde ao servidor"
                            this += player.username().colorize(Colors.COMMAND_GREEN)
                            this += "! Para jogar no servidor use o comando"
                            this += { suggestCmd("/link") { text("/link".colorize(Colors.COMMAND_GREEN))} }
                            this += "para linkar sua conta do discord e começe o processo de criação do seu perfil."
                        })
                }
                KeepAlive.playerJoined(profile?.username ?: player.username())
            }
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
                var prof = rProfile.Get(username).ok()

                BedrockCompat.ifPresent { bc ->
                    bc.getConnectionByName(username).ifPresent {
                        if (prof == null) {
                            runBlocking {
                                val standing = rProfile.ResolveBedrock(username).ok()
                                if (standing != null) {
                                    prof = when (standing) {
                                        is BedrockAccountStanding.KnownProfile -> standing.profile
                                        is BedrockAccountStanding.RenamedProfile -> standing.profile
                                        is BedrockAccountStanding.UnknownUser -> null
                                    }
                                }
                            }
                        }
                    }
                }

                if (prof != null && !KnownProfiles.contains(prof!!.id)) {
                    KnownProfiles.putIfAbsent(prof!!.id, prof!!)
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

                        Login.BannedIp -> buildLine {
                            this += buildLine(Headers.Server, "Seu IP foi banido do servidor.".colorize(Colors.ERR))
                            lineBreak()
                            this += { color(Color.GREY) { italic { literal ("Your IP was banned from the server.")}}}
                            lineBreak()
                            lineBreak()
                            this += buildLine ("IP:".toLiteral(),  address.getIpString().colorize(Colors.COMMAND_GREEN))
                        }

                        Login.BlockedIp -> buildLine {
                            this += buildLine (Headers.Server, "Seu IP foi recentemente blockeado pelo servidor.".colorize(Colors.ERR))
                            lineBreak()
                            this += { color(Color.GREY) { italic { literal ("Your IP was recently blocked by the server.")}}}
                            lineBreak()
                            lineBreak()
                            this += buildLine ("IP:".toLiteral(),  address.getIpString().colorize(Colors.COMMAND_GREEN))
                        }

                        Login.NewIp -> buildLine {
                            this += buildLine(Headers.Server, "Novo IP Detectado.".colorize(Colors.INFO))
                            this += buildLine("Permita".colorize(Colors.COMMAND_GREEN), "ou".toLiteral(), "Recuse".colorize(Colors.READ_ONLY_RED))
                            this += "na sua conta do discord.".colorize(Colors.INFO)
                            lineBreak()

                            this += { color(Color.GREY) { italic { literal ("New IP Detected. Allow/Deny on your linked discord account.")}}}
                            lineBreak()
                            lineBreak()
                            this += buildLine ("IP:".toLiteral(),  address.getIpString().colorize(Colors.COMMAND_GREEN))
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
            if (-settings.TLS) {
                "wss://"
            } else {
                "ws://"
            } + (-Config.Auth.Endpoint) + "/server/@me/ws", Api.client, -Config.Auth.ApiToken
        )
        var KnownProfiles = mutableMapOf<UUID, Profile>()
        var DiscordLinks = mutableMapOf<String, WebSocketMessage.DiscordLink>()
        var LoggedUsers = mutableSetOf<UUID>()

        val BedrockCompat : Optional<BedrockGeyserCompat> by lazy {
            if (FabricLoader.getInstance().isModLoaded("geyser-fabric")) {
                Logger.info("Geyser-Fabric Detected. Loading Geyser Support.")
                ServiceLoader.load(BedrockGeyserCompat::class.java).findFirst()
            } else {
                Optional.empty()
            }
        }

        val PlaceholderCompat: Optional<PlaceholderApiCompat> by lazy {
            if (FabricLoader.getInstance().isModLoaded("placeholder-api")) {
                Logger.info("PlaceholderAPI Detected. Loading PlaceholderAPI support.")
                ServiceLoader.load(PlaceholderApiCompat::class.java).findFirst()
            } else {
                Optional.empty()
            }
        }
    }
}
