package org.blocovermelho.bvauth.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.NameAndId;

import java.net.SocketAddress;

public class PreLoginEvent {
    public static final Event<PreLogin> PRE_LOGIN = EventFactory.createArrayBacked(PreLogin.class, listeners -> (s, a ,p) -> {
        for (PreLogin listener : listeners) {
            Component result = listener.onPreLogin(s, a, p);

            if (result != null) {
                return result;
            }
        }
        return null;
    });

    @FunctionalInterface
    public interface PreLogin {
        Component onPreLogin(MinecraftServer server, SocketAddress address, NameAndId nameAndId);
    }
}
