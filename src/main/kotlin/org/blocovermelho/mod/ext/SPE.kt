package org.blocovermelho.mod.ext

import carpet.patches.EntityPlayerMPFake
import net.minecraft.server.network.ServerPlayerEntity
import org.blocovermelho.mod.BVQuilt

fun ServerPlayerEntity.isLogged() : Boolean {
    return BVQuilt.Store.LoggedPlayers.contains(this.uuid)
}

fun ServerPlayerEntity.isBypassing() : Boolean {
    return BVQuilt.Store.BypassCidrCheck.contains(this.uuid)
}

fun ServerPlayerEntity.isCarpetBot() : Boolean {
    return this is EntityPlayerMPFake
}

fun ServerPlayerEntity.updateCommandTree() {
    this.server.playerManager.sendCommandTree(this)
}
