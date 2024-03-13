package org.blocovermelho.mod.ext

import net.minecraft.server.network.ServerPlayerEntity
import org.blocovermelho.mod.BVQuilt

fun ServerPlayerEntity.isLogged() : Boolean {
    return BVQuilt.Store.LoggedPlayers.contains(this.uuid)
}

fun ServerPlayerEntity.isBypassing() : Boolean {
    return BVQuilt.Store.BypassCidrCheck.contains(this.uuid)
}

fun ServerPlayerEntity.updateCommandTree() {
    this.server.playerManager.sendCommandTree(this)
}
