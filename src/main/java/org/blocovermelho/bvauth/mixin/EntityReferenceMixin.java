package org.blocovermelho.bvauth.mixin;

import net.minecraft.world.entity.EntityReference;
import org.blocovermelho.bvauth.BvAuthMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.UUID;

@Mixin(EntityReference.class)
public class EntityReferenceMixin {
    // Cleanest mixin possible. I like it.
    @ModifyArg(method = "readWithOldOwnerConversion", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityReference;of(Ljava/util/UUID;)Lnet/minecraft/world/entity/EntityReference;"))
    private static UUID bv$migrateUUID(UUID uuid) {
        return BvAuthMod.Companion.getUUIDMigrations().getOrDefault(uuid, uuid);
    }
}
