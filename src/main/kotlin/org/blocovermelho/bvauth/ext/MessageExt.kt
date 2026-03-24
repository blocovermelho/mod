package org.blocovermelho.bvauth.ext


import io.ktor.http.HttpStatusCode
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import org.blocovermelho.bvauth.BvAuthMod
import org.blocovermelho.bvauth.api.types.WebSocketMessage
import org.blocovermelho.bvauth.ext.Core.bracketed
import org.blocovermelho.bvauth.ext.Core.colorize
import org.blocovermelho.bvauth.ext.Core.suggestCmd
import org.blocovermelho.bvauth.ext.dsl.*
import org.blocovermelho.bvauth.impl.Err
import java.net.URI

object Colors {
    val READ_ONLY_RED = Color(0xF2706F)
    val COMMAND_GREEN = Color(0x4BF27B)
    val LINK = Color(0x5865F2)
    val INFO = Color(0xA72DDB)
    val AUTH = Color(0xD9902E)
    var ERR = Color(0xFF0000)
    val ADMIN = Color(0x7D21Bf)
}

object Headers {
    val Server: Component by lazy {
        if (BvAuthMod.Config.Server.Nome.value().isBlank()) {
            buildComponent {
                bracketed {
                    color(Colors.READ_ONLY_RED) {
                        literal("Bloco Vermelho")
                    }
                }
            }
        } else {
            buildComponent {
                bracketed {
                    color(Colors.READ_ONLY_RED) {
                        literal(BvAuthMod.Config.Server.Nome.value())
                    }
                }
            }
        }
    }


    val Login = "Login".colorize(Colors.AUTH)
    val Register = "Registrar".colorize(Colors.AUTH)
    val ChangePw = "Mudar Senha".colorize(Colors.AUTH)
    val Admin = "Admin".colorize(Colors.ERR)
    val Link = "Link".colorize(Colors.LINK)
}

object Components {
    fun suggestCommand(command: String): Component =
        buildComponent { suggestCmd("/registrar") { literal("/registrar") } }
}

fun <T> Err<T, Pair<HttpStatusCode,String>>.message (_when: String, extra: String = "") : Component
= buildLine {
    this += "Um erro aconteceu durante"
    this += _when.colorize(Color.YELLOW)
    lineBreak()
    this += this@message.error.second.colorize(Color.GREY)
}



fun WebSocketMessage.DiscordLink.message(): Component = buildLine {
    this += listOf(Headers.Server,Headers.Link)

    if (!this@message.isMember) {
        this += "A conta do discord @${this@message.discordHandle} não está no discord."
        this += "Verifique se esta é a conta correta e tente novamente."
    } else {
        val roleColor = this@message.extras?.roleColor?.let { Color.from(it) }
        val displayName = this@message.extras?.nickname ?: this@message.discordHandle
        val user = buildLine {
            this += displayName.colorize(roleColor)
            this += { bracketed (open = "(", close = ")") {  literal(this@message.extras?.roleName ?: "@everyone") } }
        }

        wrap("Olá", user, ".")
        lineBreak()
        wrap("Use o comando",  Components.suggestCommand("/registrar"), "para criar o seu perfil.")
    }
}

object Core {
    private fun TextBuilder.intersperse(separator: TextBuilder.() -> Unit, vararg actions: TextBuilder.() -> Unit) {
        actions.forEachIndexed { idx, it ->
            it()
            if (idx != actions.lastIndex) {
                separator()
            }
        }
    }

    fun TextBuilder.array(data: List<String>, action: TextBuilder.(String) -> Unit) {
        data.forEachIndexed { idx, it ->
            action(it)
            if (idx != data.lastIndex) {
                literal(", ")
            }
        }
    }

    fun String.colorize(color: Color?) : Component = buildComponent {
        color(color) {
            literal(this@colorize)
        }
    }

    fun String.toLiteral() : Component = buildComponent {
        literal(this@toLiteral)
    }

    fun TextBuilder.bracketed(
        bracketColor: Color = Color.GREY,
        innerColor: Color = Color.WHITE,
        open: String = "[",
        close: String = "]",
        action: TextBuilder.() -> Unit
    ) {
        color(bracketColor) {
            literal(open)
            color(innerColor) {
                action()
            }
            literal(close)
        }
    }

    fun TextBuilder.err(action: TextBuilder.() -> Unit) {
        bold {
            bracketed(innerColor = Colors.ERR) {
                literal("Erro")
            }
            action()
        }
    }

    fun TextBuilder.tooltip(tooltipText: TextBuilder.() -> Unit, action: TextBuilder.() -> Unit) {
        hoverEvent(HoverEvent.ShowText(buildComponent(tooltipText)), action)
    }

    fun TextBuilder.suggestCmd(value: String, action: TextBuilder.() -> Unit) {
        clickEvent(ClickEvent.SuggestCommand(value), action)
    }

    fun TextBuilder.copy(value: String, action: TextBuilder.() -> Unit) {
        clickEvent(ClickEvent.CopyToClipboard(value), action)
    }

    fun TextBuilder.openUri(value: URI, action: TextBuilder.() -> Unit) {
        clickEvent(ClickEvent.OpenUrl(value), action)
    }

    fun TextBuilder.maskedUri(uri: URI, mask: String) {
        tooltip({ uriHint(uri.toString()) }) {
            openUri(uri) {
                color(Color.BLUE) {
                    underlined {
                        literal(mask)
                    }
                }
            }
        }
    }

    fun TextBuilder.uri(uri: URI) = maskedUri(uri, uri.toString())

    fun TextBuilder.command(command: String) {
        color(Colors.COMMAND_GREEN) {
            tooltip({ suggestHint(command) }) {
                suggestCmd(command) {
                    literal(command)
                }
            }
        }
    }

    fun TextBuilder.translatableClipboard(data: String, translate: String) {
        bracketed(open = ">", close = "<", innerColor = Color.YELLOW, bracketColor = Colors.COMMAND_GREEN) {
            tooltip({ copyHint(data) }) {
                copy(data) {
                    translatable(translate)
                }
            }
        }
    }

    fun TextBuilder.maskedClipboard(data: String, mask: String) {
        bracketed(open = ">", close = "<", innerColor = Color.YELLOW, bracketColor = Colors.COMMAND_GREEN) {
            tooltip({ copyHint(data) }) {
                copy(data) {
                    literal(mask)
                }
            }
        }
    }

    fun TextBuilder.clipboard(data: String) = maskedClipboard(data, data)

    fun TextBuilder.copyHint(data: String) {
        hint("Clique para copiar", data, "para sua área de transferência")
    }

    fun TextBuilder.suggestHint(data: String) {
        hint("Clique para colocar", data, "no seu chat")
    }

    fun TextBuilder.uriHint(data: String) {
        hint("Clique para abrir", data, "no seu navegador")
    }

    fun TextBuilder.hint(heading: String, data: String, footing: String = "") {
        color(Color.BLUE) {
            literal("Dica: ")
        }
        literal("$heading ")
        color(Color.YELLOW) {
            italic {
                literal("\"$data\"")
            }
        }
        literal(" $footing")
    }
}