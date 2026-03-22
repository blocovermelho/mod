/*
 * Copyright 2023 The Quilt Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.blocovermelho.bvauth.ext.dsl

import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FontDescription
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.contents.data.DataSource
import java.util.Optional

/**
 * Marks objects as being part of QKL's Text Builder DSL.
 */
@DslMarker
public annotation class TextDsl

/**
 * A builder for a styled [Component] instance. Can be built into text with [build].
 *
 * @see [buildComponent]
 *
 * @author Cypher121
 */
@TextDsl
public class TextBuilder {
    /**
     * Current text state of the builder.
     */
    public val text: MutableComponent = Component.empty()

    /**
     * Current style state of the builder to be applied with [styleAndAppend].
     */
    public val style: StyleBuilder = StyleBuilder()

    /**
     * Applies the given [action] while setting the [StyleBuilder] property
     * described by [getProp] and [setProp] to [newValue] for the duration of the action.
     *
     * The value is then reset to its original state.
     *
     * @author Cypher121
     */
    public inline fun <T> withProp(
        newValue: T,
        getProp: StyleBuilder.() -> T,
        setProp: StyleBuilder.(T) -> Unit,
        action: TextBuilder.() -> Unit
    ) {
        val oldValue = getProp(style)
        setProp(style, newValue)

        action()

        setProp(style, oldValue)
    }

    /**
     * Applies the current [style] of the builder to the given [text]
     * and appends it to the builder's result.
     */
    @TextDsl
    public fun styleAndAppend(text: MutableComponent) {
        this.text.append(style.applyTo(text))
    }

    /**
     * Returns the [Component] result of this builder.
     */
    public fun build(): Component {
        return text
    }
}

/**
 * Builds a [Component] instance by configuring a [TextBuilder] with the given [action].
 */
@TextDsl
public inline fun buildComponent(action: TextBuilder.() -> Unit): Component {
    return TextBuilder().apply(action).build()
}

@TextDsl
inline fun component(action: TextBuilder.() -> Unit) : TextBuilder {
    return TextBuilder().apply(action)
}

/**
 * Adds a translatable text.
 *
 * @param value The translation key of the text.
 * @param args Any arguments to apply to the translatable text. Can be left
 * empty for no arguments.
 * @see StyleBuilder for action
 *
 * @author NoComment1105
 */
@TextDsl
public fun TextBuilder.translatable(value: String, vararg args: Any) {
    styleAndAppend(Component.translatable(value, *args))
}

/**
 * Adds a literal text.
 *
 * @param value The text.
 * @see StyleBuilder for action
 *
 * @author NoComment1105
 */
@TextDsl
public fun TextBuilder.literal(value: String) {
    styleAndAppend(Component.literal(value))
}

/**
 * Adds a mutable key bind text.
 *
 * @param key The key of the Key bind
 * @see StyleBuilder for action
 *
 * @author NoComment1105
 */
@TextDsl
public fun TextBuilder.keybind(key: String) {
    styleAndAppend(Component.keybind(key))
}


/**
 * @see Text.nbt
 *
 * @author NoComment1105
 */
//@TextDsl
//public fun TextBuilder.nbt(
//    pathPattern: String,
//    interpreting: Boolean,
//    separator: Optional<Component>,
//    nbt: DataSource
//) {
//    styleAndAppend(
//        Component.nbt(
//            pathPattern,
//            interpreting,
//            separator,
//            nbt
//        )
//    )
//}

/**
 * Adds a pre-existing [Component] instance.
 *
 * @param value The text to add
 * @see StyleBuilder for action
 *
 * @author NoComment1105
 */
@TextDsl
public fun TextBuilder.text(value: Component) {
    styleAndAppend(value.copy())
}

/**
 * Adds a scoreboard.
 *
 * @param name The name to add to the scoreboard
 * @param objective The objective of the scoreboard
 * @see StyleBuilder for action
 *
 * @author NoComment1105
 */
@TextDsl
public fun TextBuilder.scoreboard(name: String, objective: String) {
    styleAndAppend(Component.score(name, objective))
}

/**
 * Adds a resolvable entity selector.
 *
 * @param separator the optional separator if there's multiple matches issued from the selector
 * @param selector the selector
 * @see StyleBuilder for action
 *
 * @author NoComment1105
 */
//@TextDsl
//public fun TextBuilder.selector(selector: String, separator: Optional<Component>) {
//    styleAndAppend(Component.selector(SelectorPattern, separator))
//}

/**
 * Adds a mutable empty text.
 *
 * @see StyleBuilder for action
 *
 * @author NoComment1105
 */
@TextDsl
fun TextBuilder.empty() {
    styleAndAppend(Component.empty())
}

/**
 * Applies the [TextBuilder] [action] with [color] set to the provided value.
 *
 * @author Cypher121
 */
@TextDsl
inline fun TextBuilder.color(color: Color?, action: TextBuilder.() -> Unit) {
    withProp(color, { this.color }, { this.color = it }, action)
}

/**
 * Applies the [TextBuilder] [action] with [color] set to the provided value.
 *
 * @author alikindsys
 */
@TextDsl
inline fun TextBuilder.shadowColor(color: Color?, action: TextBuilder.() -> Unit) {
    withProp(color, { this.shadowColor }, { this.shadowColor = it }, action)
}


/**
 * Applies the [TextBuilder] [action] with [bold] set to the provided value (or enabled if no value given).
 *
 * @author Cypher121
 */
@TextDsl
inline fun TextBuilder.bold(bold: Boolean? = true, action: TextBuilder.() -> Unit) {
    withProp(bold, { this.bold }, { this.bold = it }, action)
}

/**
 * Applies the [TextBuilder] [action] with [italic] set to the provided value (or enabled if no value given).
 *
 * @author Cypher121
 */
@TextDsl
inline fun TextBuilder.italic(italic: Boolean? = true, action: TextBuilder.() -> Unit) {
    withProp(italic, { this.italic }, { this.italic = it }, action)
}

/**
 * Applies the [TextBuilder] [action] with [underlined] set to the provided value (or enabled if no value given).
 *
 * @author Cypher121
 */
@TextDsl
inline fun TextBuilder.underlined(underlined: Boolean? = true, action: TextBuilder.() -> Unit) {
    withProp(underlined, { this.underlined }, { this.underlined = it }, action)
}

/**
 * Applies the [TextBuilder] [action] with [strikethrough] set to the provided value (or enabled if no value given).
 *
 * @author Cypher121
 */
@TextDsl
inline fun TextBuilder.strikethrough(strikethrough: Boolean? = true, action: TextBuilder.() -> Unit) {
    withProp(strikethrough, { this.strikethrough }, { this.strikethrough = it }, action)
}

/**
 * Applies the [TextBuilder] [action] with [obfuscated] set to the provided value (or enabled if no value given).
 *
 * @author Cypher121
 */
@TextDsl
inline fun TextBuilder.obfuscated(obfuscated: Boolean? = true, action: TextBuilder.() -> Unit) {
    withProp(obfuscated, { this.obfuscated }, { this.obfuscated = it }, action)
}

/**
 * Applies the [TextBuilder] [action] with [clickEvent] set to the provided value.
 *
 * @author Cypher121
 */
@TextDsl
inline fun TextBuilder.clickEvent(clickEvent: ClickEvent?, action: TextBuilder.() -> Unit) {
    withProp(clickEvent, { this.clickEvent }, { this.clickEvent = it }, action)
}

/**
 * Applies the [TextBuilder] [action] with [hoverEvent] set to the provided value.
 *
 * @author Cypher121
 */
@TextDsl
inline fun TextBuilder.hoverEvent(hoverEvent: HoverEvent?, action: TextBuilder.() -> Unit) {
    withProp(hoverEvent, { this.hoverEvent }, { this.hoverEvent = it }, action)
}

/**
 * Applies the [TextBuilder] [action] with [insertion] set to the provided value.
 *
 * @author Cypher121
 */
@TextDsl
inline fun TextBuilder.insertion(insertion: String?, action: TextBuilder.() -> Unit) {
    withProp(insertion, { this.insertion }, { this.insertion = it }, action)
}

/**
 * Applies the [TextBuilder] [action] with [font] set to the provided value.
 *
 * @author Cypher121
 */
@TextDsl
inline fun TextBuilder.font(font: FontDescription?, action: TextBuilder.() -> Unit) {
    withProp(font, { this.font }, { this.font = it }, action)
}

/**
 * Applies the [TextBuilder] [action] with the selected style properties changed.
 *
 * @author Cypher121
 */
@TextDsl
fun TextBuilder.styled(
    color: Color? = style.color,
    shadowColor: Color? = style.shadowColor,
    bold: Boolean? = style.bold,
    italic: Boolean? = style.italic,
    underlined: Boolean? = style.underlined,
    strikethrough: Boolean? = style.strikethrough,
    obfuscated: Boolean? = style.obfuscated,
    clickEvent: ClickEvent? = style.clickEvent,
    hoverEvent: HoverEvent? = style.hoverEvent,
    insertion: String? = style.insertion,
    font: FontDescription? = style.font,
    action: TextBuilder.() -> Unit
) {
    color(color) {
        shadowColor(shadowColor){
            bold(bold) {
                italic(italic) {
                    underlined(underlined) {
                        strikethrough(strikethrough) {
                            obfuscated(obfuscated) {
                                clickEvent(clickEvent) {
                                    hoverEvent(hoverEvent) {
                                        insertion(insertion) {
                                            font(font) {
                                                action()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Applies the [TextBuilder] [action] using the given [style]'s properties.
 *
 * Similar to [Style.applyTo], properties that are set to null on the style will
 * not be changed from those currently set on the builder.
 *
 */
@TextDsl
fun TextBuilder.styled(style: Style, action: TextBuilder.() -> Unit) {
    styled(
        style.color?.let(Color::from) ?: this.style.color,
        style.shadowColor?.let  { Color(it) } ?: this.style.shadowColor,
        style.isBoldRaw,
        style.isItalicRaw,
        style.isUnderlinedRaw,
        style.isStrikethroughRaw,
        style.isObfuscatedRaw,
        style.clickEvent,
        style.hoverEvent,
        style.insertion,
        style.fontRaw,
        action
    )
}