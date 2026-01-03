package org.blocovermelho.bvauth.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import org.blocovermelho.bvauth.BvAuthMod;
import org.blocovermelho.bvauth.event.PreLoginEvent;
import org.spongepowered.asm.mixin.Mixin;

import org.spongepowered.asm.mixin.injection.At;

import java.net.SocketAddress;

@Mixin(ServerConfigurationPacketListenerImpl.class)
public abstract class ServerConfigurationPacketListenerImplMixin extends ServerCommonPacketListenerImpl {

    public ServerConfigurationPacketListenerImplMixin(MinecraftServer server, Connection connection, CommonListenerCookie cookie) {
        super(server, connection, cookie);
    }

    @WrapOperation(method = "handleConfigurationFinished",
    at= @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;canPlayerLogin(Ljava/net/SocketAddress;Lnet/minecraft/server/players/NameAndId;)Lnet/minecraft/network/chat/Component;"))
    public Component bv$newConnection(PlayerList instance, SocketAddress address, NameAndId nameAndId, Operation<Component> original) {
        Component check = PreLoginEvent.PRE_LOGIN.invoker().onPreLogin(server, address, nameAndId);
        if (check != null) {
            BvAuthMod.Companion.getLogger().warn("Kicking player for reason: {}", check.getString());
            return check;
        }
        return null;
    }
}
