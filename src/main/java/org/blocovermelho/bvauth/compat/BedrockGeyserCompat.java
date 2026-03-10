package org.blocovermelho.bvauth.compat;

import org.geysermc.cumulus.form.Form;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.api.util.MinecraftVersion;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface BedrockGeyserCompat {
    boolean isBedrockPlayer(UUID uuid);
    Optional<GeyserConnection> getConnectionByName(@NonNull String name);
    Optional<GeyserConnection> getConnectionByUUID(@NonNull UUID uuid);
    List<String> getSupportedBedrockVersions();
    boolean sendFormToUser(Form form, GeyserConnection connection);
}
