package org.blocovermelho.bvauth.ext.dsl

import net.minecraft.network.chat.FontDescription
import net.minecraft.network.chat.Style
import net.minecraft.resources.Identifier
import org.blocovermelho.bvauth.mixin.text.StyleAccessor

@PublishedApi
internal val Style.isBoldRaw: Boolean?
    get() = (this as StyleAccessor).isBoldRaw

@PublishedApi
internal val Style.isItalicRaw: Boolean?
    get() = (this as StyleAccessor).isItalicRaw

@PublishedApi
internal val Style.isStrikethroughRaw: Boolean?
    get() = (this as StyleAccessor).isStrikethroughRaw

@PublishedApi
internal val Style.isUnderlinedRaw: Boolean?
    get() = (this as StyleAccessor).isUnderlinedRaw

@PublishedApi
internal val Style.isObfuscatedRaw: Boolean?
    get() = (this as StyleAccessor).isObfuscatedRaw

@PublishedApi
internal val Style.fontRaw: FontDescription?
    get() = (this as StyleAccessor).fontRaw