package org.blocovermelho.mod.service

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach
import net.minecraft.server.MinecraftServer
import org.blocovermelho.mod.BVQuilt
import org.blocovermelho.mod.api.Routes
import org.blocovermelho.mod.api.handleErr
import org.blocovermelho.mod.async.CoroutineManager
import org.blocovermelho.mod.ext.isBypassing
import org.blocovermelho.mod.ext.sendErr
import org.blocovermelho.mod.ext.updateCommandTree

object IdentityVerificationService {
    suspend fun launch(server: MinecraftServer) = coroutineScope {
        BVQuilt.LOGGER.info("[IdentityVerificationService] Started.")
        BVQuilt.Store.Channels.Internal.PlayersToBeVerified.consumeEach {
            val spe = server.playerManager.getPlayer(it.uuid);

            if (spe != null && spe.isBypassing()) {
                val nonce = BVQuilt.Store.BypassCidrCheck[spe.uuid];
                if (!nonce.isNullOrEmpty()) {
                    val response = Routes.Auth.CIDR.Allow(spe.uuid, spe.ip, nonce).handleErr { err -> spe.sendErr(err, "Verificando sua identidade") } ?: return@consumeEach
                    if (response) {
                        BVQuilt.Store.BypassCidrCheck.remove(it.uuid)
                        BVQuilt.LOGGER.info("[IdentityVerificationService] Verified ${it.uuid}.")
                        NotificationService.successfulIdentity(spe)
                    } else {
                        BVQuilt.LOGGER.warn("[IdentityVerificationService] Failed Verifying ${it.uuid}.")
                        NotificationService.failedIdentity(spe)
                    }
                    spe.updateCommandTree()
                } else {
                    BVQuilt.LOGGER.error("[IdentityVerificationService] UUID ${it.uuid} had null nonce.")
                }
            }
        }
        BVQuilt.LOGGER.error("[IdentityVerificationService] Closed.")
    }

}
