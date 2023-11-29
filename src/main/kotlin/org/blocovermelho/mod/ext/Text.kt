package org.blocovermelho.mod.ext

import org.blocovermelho.mod.ext.Helpers.bracketed
import org.quiltmc.qkl.library.text.*

object Other {
    fun TextBuilder.serverHeader(action: TextBuilder.() -> Unit) {
        bracketed (innerColor = Colors.COMMAND_GREEN, open = "<", close = ">") {
            literal("Bloco Vermelho")
        }
        action()
    }
}



object Commands {
    fun TextBuilder.login( action: TextBuilder.() -> Unit ) {
        bracketed(innerColor = Colors.AUTH) {
            literal("Login")
        }
        action()
    }

    fun TextBuilder.registrar( action: TextBuilder.() -> Unit ) {
        bracketed(innerColor = Colors.AUTH) {
            literal("Registrar")
        }
        action()
    }

    fun TextBuilder.mudarSenha( action: TextBuilder.() -> Unit ) {
        bracketed(innerColor = Colors.AUTH) {
            literal("Mudar Senha")
        }
        action()
    }

    fun TextBuilder.link( action: TextBuilder.() -> Unit ) {
        bracketed(innerColor = Colors.LINK) {
            literal("Link")
        }
        action()
    }
}


object Helpers {
    fun TextBuilder.bracketed(bracketColor: Color = Color.GREY, innerColor: Color = Color.WHITE, open: String = "[", close: String = "]", action: TextBuilder.() -> Unit) {
        color(bracketColor) {
            literal(open)
            color(innerColor) {
                action()
            }
            literal(close)
        }

    }

    fun TextBuilder.err( action: TextBuilder.() -> Unit ) {
        bold {
            bracketed(innerColor = Colors.ERR) {
                literal("Erro")
            }
            action()
        }
    }

    fun TextBuilder.tooltip(tooltipText: TextBuilder.() -> Unit, action: TextBuilder.() -> Unit) {
        hoverEvent(HoverEvents.showText(buildText(tooltipText)), action)
    }

    fun TextBuilder.suggestCmd(value: String, action: TextBuilder.() -> Unit) {
        clickEvent(ClickEvents.suggestCommand(value), action)
    }

    fun TextBuilder.copy(value: String, action: TextBuilder.() -> Unit) {
        clickEvent(ClickEvents.copyToClipboard(value), action)
    }

    fun TextBuilder.openUri(value: String, action: TextBuilder.() -> Unit){
        clickEvent(ClickEvents.openUrl(value), action)
    }

    fun TextBuilder.maskedUri(uri: String, mask: String) {
        tooltip({uriHint(uri)}){
            openUri(uri) {
                color(Color.BLUE) {
                    underlined {
                        literal(mask)
                    }
                }
            }
        }
    }

    fun TextBuilder.uri(uri: String) = maskedUri(uri, uri)

    fun TextBuilder.command(command: String) {
        color(Colors.COMMAND_GREEN){
            tooltip({suggestHint(command)}) {
                suggestCmd(command) {
                    literal(command)
                }
            }
        }
    }

    fun TextBuilder.maskedClipboard(data: String, mask: String) {
        bracketed (open=">", close ="<", innerColor = Color.YELLOW, bracketColor = Colors.COMMAND_GREEN){
            tooltip({copyHint(data)}){
                copy(data) {
                    literal(mask)
                }
            }
        }
    }

    fun TextBuilder.clipboard(data: String) = maskedClipboard(data, data)

    fun TextBuilder.copyHint(data: String) {
        hint("Clique para copiar", data,"para sua área de transferência")
    }

    fun TextBuilder.suggestHint(data: String) {
        hint("Clique para colocar", data ,"no seu chat")
    }

    fun TextBuilder.uriHint(data: String) {
        hint("Clique para abrir", data ,"no seu navegador")
    }

    fun TextBuilder.hint(heading: String, data :String, footing: String = "") {
        color(Color.BLUE) {
            literal("Dica: ")
        }
        literal("$heading ")
        color(Color.YELLOW){
            italic {
                literal("\"$data\"")
            }
        }
        literal(" $footing")
    }
}
