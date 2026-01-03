package org.blocovermelho.bvauth.actor

import com.google.common.net.HttpHeaders
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.serialization.SerializationException
import org.blocovermelho.bvauth.BvAuthMod
import org.blocovermelho.bvauth.api.types.WebSocketMessage
import org.blocovermelho.bvauth.ext.Headers
import org.blocovermelho.bvauth.ext.message
import kotlin.coroutines.CoroutineContext
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class WebsocketS(val uri: String, val client: HttpClient) {
    var backoff = 1.seconds
    var last = Clock.System.now()
}

class WebsocketActor(
    val queue: ReceiveChannel<WebsocketMessage>,
    uri: String, client: HttpClient, val token: String,
    override val coroutineContext: CoroutineContext
) : CoroutineScope {
    val state = WebsocketS(uri, client)
    lateinit var session: DefaultClientWebSocketSession

    suspend fun run() {
        var flag = true
        while (flag && isActive) {
            try {
                session = state.client.webSocketSession(state.uri) {
                    header(HttpHeaders.SEC_WEBSOCKET_PROTOCOL, "Authorization=$token")
                }

                state.backoff = 1.seconds

                BvAuthMod.Logger.info("[Websocket] Connected.")

                coroutineScope {
                    val reader = launch {
                        for (frame in session.incoming) {
                            val string = frame.readBytes().decodeToString()
                            val msg = try {
                                BvAuthMod.Json.decodeFromString<WebSocketMessage>(string)
                            } catch (e: SerializationException) {
                                BvAuthMod.Logger.warn("[Websocket] Deserialization Error: $e")
                                null
                            }

                            when(msg) {
                                is WebSocketMessage.DiscordLink -> {
                                    if (msg.isMember) {
                                        BvAuthMod.DiscordLinks[msg.username] = msg
                                    }

                                    BvAuthMod.PlayerNotification?.sendPermissionLevel(msg.username)
                                    BvAuthMod.PlayerNotification?.sendMessage(msg.username, msg.message())
                                }
                                null -> TODO()
                            }


                        }
                    }

                    val control = launch {
                        for (msg in queue) {
                            when (msg) {
                                WebsocketMessage.Close -> {
                                    session.close(
                                        CloseReason(
                                            CloseReason.Codes.GOING_AWAY,
                                            "Manual closure requested."
                                        )
                                    )
                                    flag = false
                                    reader.cancelAndJoin()
                                }

                                WebsocketMessage.ReconnectNow -> {
                                    session.close(CloseReason(CloseReason.Codes.SERVICE_RESTART, "Forced Restart."))
                                    state.backoff = 1.seconds
                                    reader.cancelAndJoin()
                                }
                            }
                        }
                    }

                    joinAll(reader, control)
                }

            } catch (e: Exception) {
                BvAuthMod.Logger.warn("[Websocket] Connection failed: ${e.message}, retrying in ${state.backoff}")
                delay(state.backoff)
                state.backoff = (state.backoff * 2).coerceAtMost(1.minutes)
            }
        }
    }

    companion object {
        fun CoroutineScope.spawnWebsocket(uri: String, client: HttpClient, token: String): WebsocketActorHandle {
            val ch = Channel<WebsocketMessage>(Channel.UNLIMITED)
            val actor = WebsocketActor(ch, uri, client, token, this.coroutineContext)

            async {
                actor.run()
            }

            return WebsocketActorHandle(ch)
        }
    }
}


class WebsocketActorHandle(val queue: SendChannel<WebsocketMessage>) {
    fun close() {
        queue.trySend(WebsocketMessage.Close)
        queue.close()
    }

    fun reconnectNow() {
        queue.trySend(WebsocketMessage.ReconnectNow)
    }
}

sealed class WebsocketMessage {
    object ReconnectNow : WebsocketMessage()
    object Close : WebsocketMessage()
}