package org.blocovermelho.mod.api.ws.handlers

import org.blocovermelho.mod.BVQuilt
import org.blocovermelho.mod.api.ws.SocketEvent

suspend fun handleLinkResponse(message: SocketEvent.LINK_RESPONSE) {
    BVQuilt.LOGGER.info("[LinkResponseHandler] Got a message.")
    BVQuilt.Store.Channels.Internal.PlayersToBeLinked.send(message.data);
    BVQuilt.LOGGER.info("[LinkResponseHandler] Message sent.")
}
