package org.blocovermelho.bvauth.actor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.getOrElse
import kotlinx.coroutines.selects.onTimeout
import kotlinx.coroutines.selects.selectUnbiased
import org.blocovermelho.bvauth.BvAuthMod
import org.blocovermelho.bvauth.api.routes.GameServer
import org.blocovermelho.bvauth.api.types.KeepAlive
import kotlin.coroutines.CoroutineContext
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class KeepAliveS {
    var playerList: MutableSet<String> = mutableSetOf()
    var last: Instant = Clock.System.now()

    // FEAT: Update twice as fast if people are online.
    val interval: Duration
        get() =
            if (playerList.isNotEmpty()) {
                30.seconds
            } else {
                1.minutes
            }

    fun until(): Duration {
        return (last + interval) - Clock.System.now()
    }

    fun playerJoined(username: String) {

        BvAuthMod.Logger.info("[a: KeepAlive] Event:PlayerJoined($username)")
        playerList.add(username)
    }

    fun playerLeft(username: String) {
        BvAuthMod.Logger.info("[a: KeepAlive] Event:PlayerLeft($username)")
        playerList.remove(username)
    }
}


class KeepAliveActor(
    val queue: ReceiveChannel<KeepAliveMessage>,
    override val coroutineContext: CoroutineContext
) : CoroutineScope {
    val state = KeepAliveS()

    suspend fun run() {
        var flag = true
        while (flag) {
            selectUnbiased<Unit> {
                onTimeout(state.until()) {
                    BvAuthMod.Logger.info("[a: KeepAlive] Sent KeepAlive")
                    GameServer.KeepAlive(KeepAlive(state.playerList.toList(), null))
                    state.last = Clock.System.now()
                }

                async {
                    val result = queue.receiveCatching().getOrElse { flag = false }
                    when (result) {
                        is KeepAliveMessage.PlayerJoined -> state.playerJoined(result.username)
                        is KeepAliveMessage.PlayerLeft -> state.playerLeft(result.username)
                    }
                }
            }
        }
    }

    companion object {
        fun CoroutineScope.spawnKeepAlive(): KeepAliveActorHandle {
            val ch = Channel<KeepAliveMessage>(Channel.UNLIMITED)
            val actor = KeepAliveActor(ch, this.coroutineContext)

            async {
                actor.run()
            }
            return KeepAliveActorHandle(ch)
        }
    }
}

class KeepAliveActorHandle(val queue: SendChannel<KeepAliveMessage>) {
    fun playerJoined(username: String) {
        queue.trySend(KeepAliveMessage.PlayerJoined(username))
    }

    fun playerLeft(username: String) {
        queue.trySend(KeepAliveMessage.PlayerLeft(username))
    }
}

sealed class KeepAliveMessage {
    data class PlayerJoined(val username: String) : KeepAliveMessage()
    data class PlayerLeft(val username: String) : KeepAliveMessage()
}