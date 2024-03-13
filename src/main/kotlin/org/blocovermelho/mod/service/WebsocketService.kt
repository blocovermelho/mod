package org.blocovermelho.mod.service

import kotlinx.coroutines.*
import org.blocovermelho.mod.BVQuilt
import org.blocovermelho.mod.api.ws.Routes.handleWebsocket
import org.blocovermelho.mod.async.CoroutineManager

object WebsocketService {
    suspend fun launch() = coroutineScope {
        BVQuilt.LOGGER.info("[WebsocketService] Started.")
        handleWebsocket()
        BVQuilt.LOGGER.error("[WebsocketService] Closed.")
    }

}
