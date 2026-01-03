package org.blocovermelho.bvauth.mixin;


import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.blocovermelho.bvauth.BvAuthMod;
import org.blocovermelho.bvauth.event.PreLoginEvent;
import org.blocovermelho.bvauth.impl.IdSwapper;
import org.blocovermelho.bvauth.impl.VisitorGetter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.SocketAddress;
import java.util.*;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin implements VisitorGetter, IdSwapper {
    @Shadow
    @Final
    private MinecraftServer server;

    @Shadow
    @Final
    private Map<UUID, ServerPlayer> playersByUUID;

    @Shadow
    @Final
    private List<ServerPlayer> players;

    @Unique
    private final static HashSet<UUID> bv$visitorSet = new HashSet<>();

    @Shadow
    public abstract MinecraftServer getServer();

    @Inject(method = "save", at=@At("HEAD"), cancellable = true)
    public void bv$nonPersistVisitor(ServerPlayer player, CallbackInfo ci) {
        UUID id = player.getUUID();
        if (bv$isVisitor(id)){
            BvAuthMod.Companion.getLogger().warn("Visitor detected. Data should not persist.");
            bv$unsetVisitor(id);
            ci.cancel();
        }
    }

    @Override
    public void bv$setVisitor(UUID id) {
        bv$visitorSet.add(id);
    }

    @Override
    public boolean bv$isVisitor(UUID id) {
        return bv$visitorSet.contains(id);
    }

    @Override
    public void bv$unsetVisitor(UUID id) {
        bv$visitorSet.remove(id);
    }

    @Unique
    public void bv$swapId(UUID old, UUID _new) {
        ServerPlayer from = playersByUUID.get(old);
        ServerPlayer into = playersByUUID.get(_new);
        if (from != null && into == null) {
            // Perform Swap

            // Change the GameProfile of the User Entity.
            GameProfile prof = from.getGameProfile();
            if  (prof.properties() != null) {
                from.gameProfile = new GameProfile(_new, prof.name(), prof.properties());
            } else {
                from.gameProfile = new GameProfile(_new, prof.name());
            }

            // Update the UUID of the player.
            from.setUUID(_new);

            // Update the known ids of the level
            PersistentEntitySectionManager<Entity> entityManager = from.level().entityManager;
            Set<UUID> knownUuids = entityManager.knownUuids;
            knownUuids.add(_new);
            knownUuids.remove(old);
            entityManager.visibleEntityStorage.byUuid.put(_new, from);
            entityManager.visibleEntityStorage.byUuid.remove(old);

            // Swap entries of the PlayerList Map.
            playersByUUID.put(_new, from);
            playersByUUID.remove(old);
        }
    }
}
