package org.blocovermelho.bvauth.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class ObfuscateNameEvent {
    public static Event<ObfuscateName> REPLACE_NAME = EventFactory.createArrayBacked(ObfuscateName.class, listeners -> () -> {
        for (ObfuscateName listener : listeners) {
            return listener.replaceName();
        }
        return null;
    });
    @FunctionalInterface
    public interface ObfuscateName {
        String replaceName();
    }
}
