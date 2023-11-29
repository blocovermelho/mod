package org.blocovermelho.mod

import org.blocovermelho.mod.config.ApiSettings
import org.blocovermelho.mod.config.ServerDetails
import org.blocovermelho.mod.events.registerEvents
import org.quiltmc.loader.api.ModContainer
import org.quiltmc.loader.api.config.v2.QuiltConfig
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID

object BVQuilt : ModInitializer {
    val LOGGER: Logger = LoggerFactory.getLogger("BVMod")
    val API_CONFIG = QuiltConfig.create("bv-quilt", "api", ApiSettings::class.java)
    val SERVER_DATA = QuiltConfig.create("bv-quilt", "server", ServerDetails::class.java)
    override fun onInitialize(mod: ModContainer) {
        registerEvents()
        LOGGER.info("Hello Quilt world from {}!", mod.metadata()?.name())
    }

    object Store {
        val LoggedPlayers : MutableSet<UUID> = mutableSetOf()
        var ServerUUID : UUID = UUID.fromString(SERVER_DATA.id.value())
    }
}
