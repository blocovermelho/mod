package org.blocovermelho.bvauth.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.util.Util;
import org.blocovermelho.bvauth.BvAuthMod;
import org.blocovermelho.bvauth.event.ObfuscateNameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @WrapOperation(method = "buildPlayerStatus", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;nameAndId()Lnet/minecraft/server/players/NameAndId;"))
    public NameAndId bv$modifyPlayerListPong(ServerPlayer instance, Operation<NameAndId> original) {
        if (BvAuthMod.Companion.getConfig().Server.Obfuscar.value()) {
            String name = ObfuscateNameEvent.REPLACE_NAME.invoker().replaceName();
            return new NameAndId(Util.NIL_UUID, name);
        }
        return original.call(instance);
    }
}
