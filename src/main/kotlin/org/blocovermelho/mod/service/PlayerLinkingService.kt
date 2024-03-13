package org.blocovermelho.mod.service

import kotlinx.coroutines.async
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.minecraft.server.MinecraftServer
import org.blocovermelho.mod.BVQuilt
import org.blocovermelho.mod.api.Routes
import org.blocovermelho.mod.api.handleErr
import org.blocovermelho.mod.api.models.CreateUser
import org.blocovermelho.mod.ext.sendErr
import org.blocovermelho.mod.ext.updateCommandTree


object PlayerLinkingService {
    suspend fun launch(server: MinecraftServer) = coroutineScope {
        BVQuilt.LOGGER.info("[PlayerLinkingService] Started.")
        BVQuilt.Store.Channels.Internal.PlayersToBeLinked.consumeEach {
            val player = server.playerManager.getPlayer(it.minecraftUuid)
            if (player != null) {
                val user = CreateUser(it.minecraftUuid, player.gameProfile.name, it.discordId)
                Routes.User.Create(user).handleErr { err -> player.sendErr(err, "Criar sua conta no BV") } ?: return@consumeEach

                BVQuilt.LOGGER.info("[PlayerLinkingService] Linked (${it.discordId}) => (${player.uuid}) ")
                NotificationService.successfulLink(player, it)

                player.updateCommandTree()
            }
        }
        BVQuilt.LOGGER.error("[PlayerLinkingService] Closed.")
    }
}
