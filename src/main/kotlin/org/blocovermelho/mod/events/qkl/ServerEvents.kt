package org.blocovermelho.mod.events.qkl

import com.mojang.authlib.GameProfile
import net.minecraft.server.MinecraftServer
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import org.quiltmc.qkl.library.EventRegistration
import java.net.SocketAddress

public typealias PreLoginCallback = (server: MinecraftServer, address: SocketAddress ,  profile: GameProfile) -> Text?

public fun EventRegistration.onPreLogin(callback: PreLoginCallback) {
    callback.apply {
        org.blocovermelho.mod.event.PreLoginCallback.EVENT.register(callback)
    }
}
