package org.blocovermelho.mod.api.ws

import io.ktor.client.plugins.websocket.*
import org.blocovermelho.mod.api.BVClient
import org.blocovermelho.mod.api.ws.messages.LinkQuery
import org.blocovermelho.mod.api.ws.messages.LinkResponse
import java.util.UUID

object Routes {
    object Discord {
        suspend fun GetAccountForPlayer(playerUUID: UUID) : LinkResponse {
            lateinit var serialized: LinkResponse;

            BVClient.client.webSocket(BVClient.websocketEndpoint + "/auth/ws", {
                header(HttpHeaders.Authorization, "Bearer ${BVClient.apiConfig.token.value()}")
            }) {
                sendSerialized(LinkQuery(playerUUID))

                serialized = receiveDeserialized<LinkResponse>()
            }

            return serialized
        }
    }
}
