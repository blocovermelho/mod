package org.blocovermelho.mod.commands.admin

import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import org.blocovermelho.mod.api.Routes
import org.blocovermelho.mod.api.handleErr
import org.blocovermelho.mod.ext.Helpers.command
import org.blocovermelho.mod.ext.Helpers.translatableClipboard
import org.blocovermelho.mod.ext.Other.serverHeader
import org.blocovermelho.mod.ext.Rich.colorize
import org.blocovermelho.mod.ext.Rich.lineOf
import org.blocovermelho.mod.ext.Rich.lines
import org.blocovermelho.mod.ext.Subcommand
import org.blocovermelho.mod.ext.launch
import org.blocovermelho.mod.ext.sendError
import org.blocovermelho.mod.service.NotificationService.playerNotFound
import org.blocovermelho.mod.service.NotificationService.staffActor
import org.quiltmc.qkl.library.brigadier.argument.enum
import org.quiltmc.qkl.library.brigadier.argument.literal
import org.quiltmc.qkl.library.brigadier.argument.player
import org.quiltmc.qkl.library.brigadier.argument.value
import org.quiltmc.qkl.library.brigadier.required
import org.quiltmc.qkl.library.brigadier.util.player
import org.quiltmc.qkl.library.brigadier.util.sendFeedback
import org.quiltmc.qkl.library.text.Color
import org.quiltmc.qkl.library.text.buildText
import org.quiltmc.qkl.library.text.translatable

fun Subcommand.revokeCommand() {
    required(literal("revoke")) { _ ->
        required(enum("type", listOf("LINK", "ACCOUNT"))) { kind ->
            required(player("user")) { user ->
                launch {
                    val kindVal = kind().value()
                    val target = user().value()

                    if (kindVal.uppercase() == "LINK") {
                        revokeLink(target)
                    } else if ( kindVal.uppercase() == "ACCOUNT") {
                        revokeAccount(target)
                    }
                }
            }
        }
    }
}
suspend fun CommandContext<ServerCommandSource>.revokeLink(target: ServerPlayerEntity) {
    val account = Routes.User.Exists(target.uuid).handleErr { sendError(it, "verificando se o link existe") }
        ?: return

    if (!account) {
        sendFeedback {
            buildText {
                serverHeader {
                    playerNotFound(target)
                }
            }
        }
    }

    val delete = Routes.User.Delete(target.uuid).handleErr { sendError(it, "apagando o link") }
        ?: return

    target.sendSystemMessage(buildText {
        serverHeader {
            lines(
                { translatable("bv.unlink.self.success") },
                { staffActor(target) },
                {
                    lineOf(
                        { translatable("bv.action.clipboard") },
                        { translatableClipboard("<@${delete.discordId}>","bv.mask.discord_id") },
                        { translatable("bv.unlink.see_previous") }
                    )
                }
            )
        }

        serverHeader {
            lineOf(
                { translatable("bv.action.use", buildText { command("/link") }) },
                { translatable("bv.link.hint")}
            )
        }
    })

    sendFeedback {
        buildText {
            serverHeader {
                translatable("bv.unlink.of.success", colorize(target.gameProfile.name, Color.YELLOW))
            }
        }
    }
}

suspend fun CommandContext<ServerCommandSource>.revokeAccount(target: ServerPlayerEntity) {
    val account = Routes.Auth.Exists(target.uuid).handleErr { sendError(it, "verificando se a conta existe") }
        ?: return


    if (!account) {
        sendFeedback {
            buildText {
                playerNotFound(target)
            }
        }
    }

    Routes.Auth.Delete(target.uuid).handleErr { sendError(it, "apagando a conta") }
        ?: return

    target.sendSystemMessage(buildText {
        serverHeader {
            lines(
                { translatable("bv.revoke.self.success") },
                { staffActor(player!!) }
            )
        }

        serverHeader {
            lineOf(
                { translatable("bv.action.use", buildText { command("/registrar") }) },
                { translatable("bv.register.hint") }
            )
        }
    })

    sendFeedback {
        buildText {
            serverHeader {
                translatable("bv.revoke.of.success", colorize(target.gameProfile.name, Color.YELLOW))
            }
        }
    }
}
