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
import org.blocovermelho.mod.ext.isCarpetBot
import org.blocovermelho.mod.service.NotificationService
import org.quiltmc.qkl.library.text.buildText
import java.net.SocketAddress
import java.util.*

suspend fun onLoginAttempt(server: MinecraftServer, address: SocketAddress, profile: GameProfile) : Text? {
    BVQuilt.LOGGER.info("[PreLoginEvent] Processing ${address.address} for ${profile.id}")
    var text: Text? = null;
    val playerManager = server.playerManager;

    val onlinePlayer = server.playerManager.getPlayer(profile.id)
    val isCarpet = onlinePlayer?.isCarpetBot() ?: false;
    val isOnline = onlinePlayer != null;
    val isLoggedIn = BVQuilt.Store.LoggedPlayers.contains(profile.id);

    val banReason = if (BVQuilt.Store.BanReasons.isEmpty()) { "Falsidade Ideológica" } else { BVQuilt.Store.BanReasons.random() }

    if (isCarpet) {
        BVQuilt.LOGGER.info("[PreLoginEvent] Tried replacing a Carpet Bot")
        // This is a manual ban, issued to a random uuid. You'd need to actually ask for forgiveness
        // for this to be removed since I said so. It is a ban which was issued automatically, but
        // it will be needed to be removed manually. Trying to fuck up the bots as a joke is stupid.
        Routes.Auth.CIDR.Ban(onlinePlayer!!.uuid, address.address, UUID.randomUUID())
        NotificationService.newBan(banReason, playerManager, profile, address)
        // There won't ever be an account for a carpet bot.
        return buildText { banMessage(banReason, profile.name) }
    }

    val user = Routes.Auth.Exists(profile.id).handleErr {  } ?: return Text.of("Internal API Error");

    if (!user) {
        BVQuilt.LOGGER.info("[PreLoginEvent] Account for ${profile.id} not found")
        return null
    }

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

    val serverUuid = UUID.fromString(BVQuilt.SERVER_DATA.id.value());
    val check = Routes.Auth.CIDR.Check(profile.id, serverUuid, address.address).handleErr {  } ?: return Text.of("Internal API Error");

    text = when (check) {
        Cidr.Response.Allowed -> null
        Cidr.Response.Banned -> buildText {
            banMessage(banReason, profile.name)
        }
        Cidr.Response.Unknown -> buildText {
            newIdentity()
        }
    }

    if (text != null) {
        BVQuilt.LOGGER.info("[PreLoginEvent] ${profile.id} was banned. Disconnection should follow.")
    }

    return text
}
