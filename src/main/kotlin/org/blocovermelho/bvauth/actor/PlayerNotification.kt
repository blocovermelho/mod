package org.blocovermelho.bvauth.actor

import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.isActive
import net.minecraft.network.chat.Component
import net.minecraft.server.players.PlayerList
import kotlin.coroutines.CoroutineContext

class PlayerNotificationS(val playerList: PlayerList) {
    fun sendMessage(username: String, component: Component) {
        playerList.getPlayer(username)?.sendSystemMessage(component)
    }

    fun sendPermissionLevel(username: String) {
        val player = playerList.getPlayer(username)
        if (player != null) {
            playerList.sendPlayerPermissionLevel(player)
        }
    }
}

sealed class PlayerNotificationMessage() {
    data class SendMessage(val username: String, val component: Component): PlayerNotificationMessage()
    data class SendPermissionLevel(val username: String) : PlayerNotificationMessage()
}

class PlayerNotificationActor(val queue: ReceiveChannel<PlayerNotificationMessage>, playerList: PlayerList,
                              override val coroutineContext: CoroutineContext
) : CoroutineScope  {
    val state = PlayerNotificationS(playerList)

    suspend fun run() {
        val flag = true
        while (flag && isActive) {
            for (msg in queue) {
                when (msg) {
                    is PlayerNotificationMessage.SendMessage -> state.sendMessage(msg.username, msg.component)
                    is PlayerNotificationMessage.SendPermissionLevel -> state.sendPermissionLevel(msg.username)
                }
            }
        }
    }

    companion object {
        fun CoroutineScope.spawnPlayerNotification(playerList: PlayerList): PlayerNotificationActorHandle {
            val ch = Channel<PlayerNotificationMessage>(Channel.UNLIMITED)
            val actor = PlayerNotificationActor(ch, playerList, this.coroutineContext)

            async {
                actor.run()
            }

            return PlayerNotificationActorHandle(ch)
        }
    }
}

class PlayerNotificationActorHandle (val queue: SendChannel<PlayerNotificationMessage>) {
    fun sendMessage(username: String, component: Component) {
        queue.trySend(PlayerNotificationMessage.SendMessage(username, component))
    }
    fun sendPermissionLevel(username: String) {
        queue.trySend(PlayerNotificationMessage.SendPermissionLevel(username))
    }
}