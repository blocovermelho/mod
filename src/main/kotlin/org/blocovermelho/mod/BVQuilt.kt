package org.blocovermelho.mod

import kotlinx.coroutines.channels.Channel
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.blocovermelho.mod.BVQuilt.Store.BanReasons
import org.blocovermelho.mod.api.BVClient
import org.blocovermelho.mod.api.models.*
import org.blocovermelho.mod.api.ws.SocketEvent
import org.blocovermelho.mod.api.ws.messages.LinkResponse
import org.blocovermelho.mod.api.ws.messages.LinkResult
import org.blocovermelho.mod.commands.registerCommands
import org.blocovermelho.mod.config.ApiSettings
import org.blocovermelho.mod.config.ServerDetails
import org.blocovermelho.mod.events.registerEvents
import org.quiltmc.loader.api.ModContainer
import org.quiltmc.loader.api.QuiltLoader
import org.quiltmc.loader.api.config.v2.QuiltConfig
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

object BVQuilt : ModInitializer {
    val LOGGER: Logger = LoggerFactory.getLogger("BVMod")
    val API_CONFIG = QuiltConfig.create("bv-quilt", "api", ApiSettings::class.java)
    val SERVER_DATA = QuiltConfig.create("bv-quilt", "server", ServerDetails::class.java)
    val GRINGO_DATA_PATH = QuiltLoader.getConfigDir().resolve("bv-quilt").resolve("gringo.json")
    override fun onInitialize(mod: ModContainer) {
        BVClient.init(API_CONFIG)
        registerEvents()
        registerCommands()
        Store.read()
        LOGGER.info("Hello Quilt world from {}!", mod.metadata()?.name())

        val str = Json.decodeFromString<ServerJoin?>("{\"Resume\":{\"loc\":{\"dim\":\"minecraft:overworld\",\"x\":209.9735469368637,\"y\":-60.0,\"z\":-231.2604255853168},\"yaw\":51.899131774902344,\"pitch\":-2.9999828338623047}}")

        LOGGER.info(str.toString())
    }

    object Store {
        val LoggedPlayers : MutableSet<UUID> = mutableSetOf()
        var ServerUUID : UUID = UUID.fromString(SERVER_DATA.id.value())
        var UnknownIp: MutableSet<UUID> = mutableSetOf()
        var BanReasons: MutableSet<String> = mutableSetOf();

        object Channels {
            object Outgoing {
                var Messages = Channel<SocketEvent>(Channel.UNLIMITED)
            }
            object Internal {
                var PlayersToBeVerified = Channel<User>(Channel.UNLIMITED)
                var PlayersToBeLinked = Channel<LinkResult>(Channel.UNLIMITED);
            }
        }

        fun read() {
            if (!GRINGO_DATA_PATH.exists()) {
                GRINGO_DATA_PATH.createFile()
                GRINGO_DATA_PATH.writeText("[]")
            }

            val gringoText = GRINGO_DATA_PATH.readText()
            BanReasons = Json.decodeFromString(gringoText)
        }

        fun flush() {
            val gringoText = Json.encodeToString(BanReasons)
            GRINGO_DATA_PATH.writeText(gringoText)
        }
    }
}
