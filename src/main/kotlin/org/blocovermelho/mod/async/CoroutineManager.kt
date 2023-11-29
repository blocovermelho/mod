package org.blocovermelho.mod.async

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import net.minecraft.server.MinecraftServer
import org.blocovermelho.mod.BVQuilt

object CoroutineManager {
    lateinit var dispatcher: CoroutineDispatcher;
    lateinit var scope: CoroutineScope;

    fun init(server: MinecraftServer) {
        dispatcher = server.asCoroutineDispatcher()
        scope = CoroutineScope(SupervisorJob() + dispatcher)
        BVQuilt.LOGGER.info("Initialized Coroutine Manager.")
    }
}
