package org.blocovermelho.mod.ext

import carpet.patches.EntityPlayerMPFake
import net.minecraft.server.network.ServerPlayerEntity
import org.blocovermelho.mod.BVQuilt

fun ServerPlayerEntity.isLogged() : Boolean {
    return BVQuilt.Store.LoggedPlayers.contains(this.uuid)
}

fun ServerPlayerEntity.isKnown() : Boolean {
    return !BVQuilt.Store.UnknownIp.contains(this.uuid)
}

fun ServerPlayerEntity.isCarpetBot() : Boolean {
    return this is EntityPlayerMPFake
}

fun ServerPlayerEntity.updateCommandTree() {
    this.server.playerManager.sendCommandTree(this)
}
