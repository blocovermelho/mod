package org.blocovermelho.mod.api.ws.handlers.cidr

import org.blocovermelho.mod.BVQuilt
import org.blocovermelho.mod.api.ws.SocketEvent

suspend fun handleCidrSynAwk(message: SocketEvent.CIDR_SYN_AWK) {
    // Yep.
    BVQuilt.LOGGER.info("[SynAwkHandler] Message received.")
    BVQuilt.Store.Channels.Internal.PlayersToBeVerified.send(message.data)
    BVQuilt.LOGGER.info("[SynAwkHandler] Message sent.")
}
