package org.blocovermelho.mod.events

import com.mojang.authlib.GameProfile
import io.ktor.util.network.*
import net.minecraft.server.MinecraftServer
import net.minecraft.text.Text
import org.blocovermelho.mod.BVQuilt
import org.blocovermelho.mod.api.Routes
import org.blocovermelho.mod.api.handleErr
import org.blocovermelho.mod.api.models.Ban
import org.blocovermelho.mod.api.models.Cidr
import org.blocovermelho.mod.ext.Mimicry.BanHammer.banMessage
import org.blocovermelho.mod.ext.Other.newIdentity
import org.blocovermelho.mod.service.NotificationService
import org.quiltmc.qkl.library.text.buildText
import java.net.SocketAddress

suspend fun onLoginAttempt(server: MinecraftServer, address: SocketAddress, profile: GameProfile) : Text? {
    BVQuilt.LOGGER.info("[PreLoginEvent] Processing ${address.address} for ${profile.id}")
    var text: Text? = null;
    val playerManager = server.playerManager;

    val user = Routes.User.Exists(profile.id).handleErr {  } ?: return Text.of("Internal API Error");

    if (!user) {
        BVQuilt.LOGGER.info("[PreLoginEvent] User for ${profile.id} not found")
        return null
    }

    if (BVQuilt.Store.BypassCidrCheck.contains(profile.id)) {
        BVQuilt.LOGGER.info("[PreLoginEvent] ${profile.id} is bypassing the CIDR check")
        return null
    }

    val isOnline = server.playerManager.getPlayer(profile.id) != null;
    val isLoggedIn = BVQuilt.Store.LoggedPlayers.contains(profile.id);
    val banReason = if (BVQuilt.Store.BanReasons.isEmpty()) { "Falsidade Ideológica" } else { BVQuilt.Store.BanReasons.random() }

    if (isOnline && isLoggedIn) {
        BVQuilt.LOGGER.info("[PreLoginEvent] ${profile.id} is already online and logged in.")
        val kind = Routes.Auth.CIDR.Ban(profile.id, address.address).handleErr {  } ?: return Text.of("Internal API Error");
        when (kind) {
            Ban.Response.Existing -> {}
            Ban.Response.Merged -> {
                BVQuilt.LOGGER.info("[PreLoginEvent] ${profile.id} was merged with another ledger entry")
                NotificationService.mergedBan(banReason, playerManager, profile, address)
            }
            Ban.Response.New ->  {
                BVQuilt.LOGGER.info("[PreLoginEvent] ${profile.id} is a new entry to the ledger")
                NotificationService.newBan(banReason, playerManager, profile, address)
            }
            Ban.Response.Invalid -> {}
        }
    }

    val check = Routes.Auth.CIDR.Check(profile.id, address.address).handleErr {  } ?: return Text.of("Internal API Error");

    text = when (check) {
        Cidr.Response.Allowed -> null
        Cidr.Response.Banned -> buildText {
            banMessage(banReason, profile.name)
        }
        Cidr.Response.Unknown ->  {
            Routes.Auth.CIDR.Ban(profile.id, address.address).handleErr {  };
            return buildText { newIdentity() }
        }
    }

    if (text != null) {
        BVQuilt.LOGGER.info("[PreLoginEvent] ${profile.id} was banned. Disconnection should follow.")
    }

    return text
}
