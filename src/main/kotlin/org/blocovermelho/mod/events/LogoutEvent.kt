package org.blocovermelho.mod.events

import net.minecraft.server.network.ServerPlayerEntity
import org.blocovermelho.mod.BVQuilt
import org.blocovermelho.mod.api.Routes
import org.blocovermelho.mod.api.handleErr
import org.blocovermelho.mod.api.models.Pos

suspend fun handleDisconnect(player: ServerPlayerEntity) {
    val removed = BVQuilt.Store.LoggedPlayers.remove(player.uuid)

    if(!removed) return

    val u = Routes.User.Exists(player.uuid).handleErr { } ?: return
    val acc = Routes.Auth.Exists(player.uuid).handleErr { } ?: return

    if (!acc || !u) {
        return
    }

    Routes.Auth.Logoff(
        BVQuilt.Store.ServerUUID, player.uuid, player.ip, Pos(
            player.blockX,
            player.blockZ,
            player.blockY,
            player.world.dimensionKey.value.toString()
        )
    )
}
