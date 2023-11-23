package org.blocovermelho.mod

import org.quiltmc.loader.api.ModContainer
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object BVQuilt : ModInitializer {
    val LOGGER: Logger = LoggerFactory.getLogger("BVMod")

    override fun onInitialize(mod: ModContainer) {
        LOGGER.info("Hello Quilt world from {}!", mod.metadata()?.name())
    }
}
