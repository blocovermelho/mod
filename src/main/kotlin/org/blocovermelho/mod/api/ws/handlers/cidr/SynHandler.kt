package org.blocovermelho.mod.api.ws.handlers.cidr

import org.blocovermelho.mod.BVQuilt
import org.blocovermelho.mod.api.ws.SocketEvent
import java.util.UUID

suspend fun handleCidrSyn(message: SocketEvent.CIDR_SYN) {
    // Yes. That's it.
    BVQuilt.LOGGER.info("[SynHandler] Message received.")
    BVQuilt.Store.BypassCidrCheck[UUID.fromString(message.data)!!] = ""
    BVQuilt.LOGGER.info("[SynHandler] Message Sent.")
}
