package org.blocovermelho.mod.api.ws

import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.blocovermelho.mod.BVQuilt
import org.blocovermelho.mod.api.BVClient
import org.blocovermelho.mod.api.ws.handlers.handleLinkResponse

object Routes {
    private suspend fun DefaultClientWebSocketSession.sendMessages() {
        BVQuilt.LOGGER.info("[Websocket-Send-Thread] Initialized")
        while (true) {
            BVQuilt.Store.Channels.Outgoing.Messages.consumeEach {
                BVQuilt.LOGGER.info("[Websocket-Send-Thread] Got Socket Event")
                outgoing.send(Frame.Text(Json.encodeToString(SocketEventSerializer, it)))
            }
        }
    }

    private suspend fun DefaultClientWebSocketSession.readMessages() {
        BVQuilt.LOGGER.info("[Websocket-Read-Thread] Initialized.")
        while (true) {
            incoming.consumeEach {
                val string = it.readBytes().decodeToString()
                BVQuilt.LOGGER.info("[Websocket-Read-Thread] Frame Data: $string")
                val serialized = try {
                    Json.decodeFromString(SocketEventSerializer, string);
                } catch (err: SerializationException ) {
                    BVQuilt.LOGGER.error("[Websocket-Read-Thread] SerializationException: $err")
                    null
                }

                BVQuilt.LOGGER.info("[Websocket-Read-Thread] Could Serialize: ${serialized != null}")

                when(serialized) {
                    is SocketEvent.LINK_RESPONSE -> handleLinkResponse(serialized)
                    else -> {
                        BVQuilt.LOGGER.info("[Websocket-Read-Thread] Unknown message received.")
                    }
                }
            }
        }
    }
    suspend fun handleWebsocket() {
        BVClient.client.webSocket(BVClient.websocketEndpoint + "/auth/ws", {
            header(HttpHeaders.Authorization, "Bearer ${BVClient.apiConfig.token.value()}")
        }) {
            val readThread = launch { readMessages() }
            val sendThread = launch { sendMessages() }

            readThread.join()
            sendThread.cancelAndJoin()
        }
    }
}
