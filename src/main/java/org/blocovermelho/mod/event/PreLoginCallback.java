package org.blocovermelho.mod.event;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.net.SocketAddress;

public interface PreLoginCallback {
	Event<PreLoginCallback> EVENT = EventFactory.createArrayBacked(PreLoginCallback.class, (listeners) -> (server, address, profile) -> {
		for (PreLoginCallback listener : listeners) {
			Text result = listener.verify(server, address, profile);

			if(result != null) {
				return result;
			}
		}

		return null;
	});

	Text verify(MinecraftServer server, SocketAddress address, GameProfile profile);
}
