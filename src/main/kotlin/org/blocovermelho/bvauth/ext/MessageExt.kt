package org.blocovermelho.bvauth.ext


import eu.pb4.placeholders.api.parsers.TagParser
import io.ktor.http.HttpStatusCode
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextColor
import net.minecraft.server.level.ServerPlayer
import org.blocovermelho.bvauth.BvAuthMod
import org.blocovermelho.bvauth.api.types.WebSocketMessage
import org.blocovermelho.bvauth.ext.STFBuilder.asComponent
import org.blocovermelho.bvauth.ext.STFBuilder.bracketed
import org.blocovermelho.bvauth.ext.STFBuilder.color
import org.blocovermelho.bvauth.ext.STFBuilder.showText
import org.blocovermelho.bvauth.ext.STFBuilder.suggestCommand
import org.blocovermelho.bvauth.impl.Err
import org.blocovermelho.bvauth.impl.HTTPReply

object Colors {
    val READ_ONLY_RED = TextColor.fromRgb(0xF2706F)
    val COMMAND_GREEN = TextColor.fromRgb(0x4BF27B)
    val LINK = TextColor.fromRgb(0x5865F2)
    val INFO = TextColor.fromRgb(0xA72DDB)
    val AUTH = TextColor.fromRgb(0xD9902E)
    var ERR = TextColor.fromRgb(0xFF0000)
    val ADMIN = TextColor.fromRgb(0x7D21Bf)
}

object Headers {
    val Server: String
        get() = if (BvAuthMod.Config.Server.Nome.isBlank()) {
            "Bloco Vermelho".color(Colors.READ_ONLY_RED).bracketed()
        } else {
            BvAuthMod.Config.Server.Nome.color(Colors.READ_ONLY_RED).bracketed()
        }


    val Login = "Login".color(Colors.AUTH)
    val Register = "Registrar".color(Colors.AUTH)
    val ChangePw = "Mudar Senha".color(Colors.AUTH)

    val Link = "Link".color(Colors.LINK)
}

fun <T> Err<T, Pair<HttpStatusCode,String>>.message (_when: String, extra: String = "") : String {
    return listOf("Um erro aconteceu durante",_when.color(ChatFormatting.YELLOW), "$extra\n", this.error.second.color(ChatFormatting.GRAY)).joinToString (" ")
}


fun WebSocketMessage.DiscordLink.message(): Component {
    if (!this.isMember) {
        val user = this.username
        return listOf(
            Headers.Server,
            Headers.Link,
            "A conta do discord @${this.discordHandle} não está no discord.",
            "Verifique se esta é a conta correta e tente novamente."
        ).joinToString(" ").asComponent()
    } else {
        val user = (this.extras?.nickname ?: this.discordHandle).color(
            this.extras?.roleColor ?: "#FFFFF"
        ) + " " + "(${this.extras?.roleName ?: "@everyone"})"

        return listOf(
            listOf(Headers.Server, Headers.Link, "Olá $user.").joinToString(" "), listOf(
                "Use o comando",
                "/registrar".suggestCommand("/registrar").color(Colors.COMMAND_GREEN)
                    .showText("Clique para colocar o comando no seu chat."),
                "para criar seu perfil."
            ).joinToString(" ")
        ).joinToString("\n").asComponent()
    }
}

object STFBuilder {

    fun String.asUrl(): String {
        return this.color(Colors.LINK).underlined().openUrl(this)
    }

    fun String.maskedUrl(mask: String): String {
        return mask.color(Colors.LINK).underlined().openUrl(this)
    }

    fun String.bracketed(
        start: String = "[",
        end: String = "]",
        color: TextColor = TextColor.fromRgb(0xFFFFFF)
    ): String {
        return "[".color(color) + this + "]".color(color)
    }

    fun String.color(color: String) : String {
        return "<c $color>$this</c>"
    }

    fun String.color(color: TextColor): String {
        return "<c ${color.formatValue()}>$this</c>"
    }

    fun String.color(color: ChatFormatting): String {
        return "<c ${TextColor.fromLegacyFormat(color)?.formatValue()}>$this</c>"
    }

    fun String.bold(): String {
        return "<b>$this</b>"
    }

    fun String.italic(): String {
        return "<i>$this</i>"
    }

    fun String.underlined(): String {
        return "<underlined>$this</underlined>"
    }


    fun String.obfuscated(): String {
        return "<obf>$this</obf>"
    }


    fun String.strikethrough(): String {
        return "<st>$this</st>"
    }

    fun String.openUrl(url: String): String {
        return "<url '$url'>$this</url>"
    }

    fun String.runCommand(command: String): String {
        return "<run_cmd '$command'>$this</run_cmd>"
    }

    fun String.suggestCommand(command: String): String {
        return "<cmd '$command'>$this</cmd>"
    }

    fun String.copyToClipboard(text: String): String {
        return "<copy '$text'>$this</copy>"
    }

    fun String.showText(text: String): String {
        return "<hover '$text'>$this</hover>"
    }

    fun String.block(block: String): String {
        return "<atlas atlas:blocks' texture:'$block'>$this</cmd>"
    }

    fun String.item(item: String): String {
        return "<atlas atlas:items' texture:'$item'>$this</cmd>"
    }

    fun String.player(player: String): String {
        return "<atlas name:'$player' hat:true> $this"
    }

    fun String.gradient(vararg color: TextColor): String {
        return "<gr ${color.joinToString(" ") { it.formatValue() }}>$this</gr>"
    }

    fun String.gradient(vararg color: String): String {
        return "<gr ${color.joinToString (" " )}>$this</gr>"
    }

    fun String.asComponent(): Component {
        return TagParser.QUICK_TEXT.parseNode(this).toText()
    }
}