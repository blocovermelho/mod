package org.blocovermelho.bvauth.event;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;


import java.net.SocketAddress;
import java.util.UUID;

public class IdentityResolveEvent {
    public static Event<IdentityResolve> IDENTITY_RESOLVE = EventFactory.createArrayBacked(IdentityResolve.class, listeners -> (addr, prof) -> {
       for (IdentityResolve listener : listeners) {
           return listener.onIdentityResolve(addr, prof);
       }
       return null;
    });

    @FunctionalInterface
    public interface IdentityResolve {
        UUID onIdentityResolve(String address, String username);
    }
}
