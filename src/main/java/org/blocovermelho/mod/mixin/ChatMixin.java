package org.blocovermelho.mod.mixin;

import net.minecraft.network.packet.c2s.play.ChatCommandC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.blocovermelho.mod.events.SendMessageEventKt.onPlayerChat;

@Mixin(ServerPlayNetworkHandler.class)
public class ChatMixin {

	@Shadow
	public ServerPlayerEntity player;

	@Inject(method="onChatCommand", at= @At(value = "HEAD"), cancellable = true)
	private void onPlayerCommand(ChatCommandC2SPacket packet, CallbackInfo ci) {
		var result = onPlayerChat(player, packet.command());

		if(result == ActionResult.FAIL) {
			ci.cancel();
		}
	}

}
