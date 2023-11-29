package org.blocovermelho.mod.api.ws

import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.blocovermelho.mod.api.BVClient
import org.blocovermelho.mod.api.ws.messages.Error
import org.blocovermelho.mod.api.ws.messages.Exists
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

                val bytes = incoming.receive().readBytes()
                val str = bytes.decodeToString()

                serialized = try {
                    Json.decodeFromString<Exists>(str)
                } catch (err: SerializationException) {
                    Json.decodeFromString<Error>(str)
                }
            }

            return serialized
        }
    }
}
