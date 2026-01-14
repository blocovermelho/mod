package org.blocovermelho.bvauth.mixin.text;

import net.minecraft.network.chat.*;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Style.class)
public interface StyleAccessor {
    @Invoker("<init>")
    static Style create(
            @Nullable TextColor color,
            @Nullable Integer shadowColor,
            @Nullable Boolean bold,
            @Nullable Boolean italic,
            @Nullable Boolean underlined,
            @Nullable Boolean strikethrough,
            @Nullable Boolean obfuscated,
            @Nullable ClickEvent clickEvent,
            @Nullable HoverEvent hoverEvent,
            @Nullable String insertion,
            @Nullable FontDescription font
    ) {
        throw new UnsupportedOperationException();
    }

    @Accessor("bold")
    @Nullable
    Boolean isBoldRaw();

    @Accessor("italic")
    @Nullable
    Boolean isItalicRaw();

    @Accessor("underlined")
    @Nullable
    Boolean isUnderlinedRaw();

    @Accessor("strikethrough")
    @Nullable
    Boolean isStrikethroughRaw();

    @Accessor("obfuscated")
    @Nullable
    Boolean isObfuscatedRaw();

    @Accessor("font")
    @Nullable
    FontDescription getFontRaw();
}
