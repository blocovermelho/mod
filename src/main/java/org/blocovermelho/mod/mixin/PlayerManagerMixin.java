package org.blocovermelho.mod.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.text.Text;
import org.blocovermelho.mod.event.PreLoginCallback;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.SocketAddress;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
	@Shadow
	@Final
	private MinecraftServer server;

	@Inject(method = "checkCanJoin", at = @At("HEAD"), cancellable = true)
	public void bv$newConnection(SocketAddress address, GameProfile profile, CallbackInfoReturnable<Text> cir) {
		Text verify = PreLoginCallback.EVENT.invoker().verify(server,address, profile);
		if (verify != null) {
			cir.setReturnValue(verify);
		}
	}
}
