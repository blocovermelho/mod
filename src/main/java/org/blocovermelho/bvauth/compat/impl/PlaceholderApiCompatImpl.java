package org.blocovermelho.bvauth.compat.impl;

import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.objects.AtlasSprite;
import net.minecraft.resources.Identifier;
import org.blocovermelho.bvauth.compat.BedrockGeyserCompat;
import org.blocovermelho.bvauth.compat.PlaceholderApiCompat;

public class PlaceholderApiCompatImpl implements PlaceholderApiCompat {

    static final Identifier BEDROCK_STATUS = Identifier.fromNamespaceAndPath("bvauth", "bedrock_status");
    @Override
    public void registerBedrockPlaceholder(BedrockGeyserCompat bedrockCompat) {
        Placeholders.registerServer(BEDROCK_STATUS, (ctx, arg) -> {
            if (!ctx.hasPlayer()) {
                return PlaceholderResult.invalid("No player!");
            }


            if (bedrockCompat.getConnectionByName(ctx.player().gameProfile.name()).isPresent()) {
                return PlaceholderResult.value(Component.object(new AtlasSprite(Identifier.fromNamespaceAndPath("minecraft", "blocks"), Identifier.fromNamespaceAndPath("minecraft", "block/bedrock"))));
            } else {
                return PlaceholderResult.value("");
            }
        });
    }

    ///  Exists so that placeholders aren't resolved on the case there is no compatibility
    @Override
    public void registerDummyPlaceholders() {
        Placeholders.registerServer(BEDROCK_STATUS,  (ctx, arg) -> PlaceholderResult.value(""));
    }
}
