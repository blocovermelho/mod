package org.blocovermelho.bvauth.compat.impl;

import org.blocovermelho.bvauth.compat.BedrockGeyserCompat;
import org.geysermc.cumulus.form.Form;
import org.geysermc.geyser.api.GeyserApi;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.api.util.MinecraftVersion;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import java.util.stream.Collectors;

public class BedrockGeyserCompatImpl implements BedrockGeyserCompat {
    @Override
    public boolean isBedrockPlayer(@NonNull UUID uuid) {
        return GeyserApi.api().isBedrockPlayer(uuid);
    }

    @Override
    public Optional<GeyserConnection> getConnectionByName(@NonNull String name) {
        return (Optional<GeyserConnection>) GeyserApi.api().onlineConnections().stream().filter(it -> it.bedrockUsername().equals( name)).findFirst();
    }

    @Override
    public Optional<GeyserConnection> getConnectionByUUID(@NonNull UUID uuid) {
        return Optional.ofNullable(GeyserApi.api().connectionByUuid(uuid));
    }



    @Override
    public List<String> getSupportedBedrockVersions() {
        return GeyserApi.api()
                .supportedBedrockVersions()
                .stream()
                .map(MinecraftVersion::versionString)
                .collect(Collectors.toList());
    }

    @Override
    public boolean sendFormToUser(Form form, GeyserConnection connection) {
        return connection.sendForm(form);
    }
}
