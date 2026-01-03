package org.blocovermelho.bvauth.mixin;

import com.mojang.authlib.GameProfile;
import kotlin.uuid.Uuid;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.network.Connection;
import org.blocovermelho.bvauth.BvAuthMod;
import org.blocovermelho.bvauth.event.IdentityResolveEvent;
import org.blocovermelho.bvauth.impl.IdSwapper;
import org.blocovermelho.bvauth.impl.VisitorGetter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.UUID;


@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPacketListenerImplMixin implements VisitorGetter {


    @Shadow
    @Final
    MinecraftServer server;

    @Shadow
    @Final
    Connection connection;

    @ModifyArg(method = "handleHello", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerLoginPacketListenerImpl;startClientVerification(Lcom/mojang/authlib/GameProfile;)V",
            ordinal = 1), index = 0)
    private GameProfile vb$inject_api_uuid(GameProfile authenticatedProfile) {

        String ip = ((InetSocketAddress)connection.getRemoteAddress()).getAddress().getHostAddress();

        UUID uuid = IdentityResolveEvent.IDENTITY_RESOLVE.invoker().onIdentityResolve(ip, authenticatedProfile.name());

        if (uuid != null) {
            if (authenticatedProfile.properties() != null) {
                return new GameProfile(uuid, authenticatedProfile.name(), authenticatedProfile.properties());
            } else {
                return new GameProfile(uuid, authenticatedProfile.name());
            }
        }

        BvAuthMod.Companion.getLogger().warn("Visitor Joined. Adding to list.");

        ((VisitorGetter)server.getPlayerList()).bv$setVisitor(authenticatedProfile.id());


        return authenticatedProfile;
    }


}
