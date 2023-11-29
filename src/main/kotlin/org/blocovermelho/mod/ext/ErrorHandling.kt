package org.blocovermelho.mod.ext

import com.mojang.brigadier.context.CommandContext
import kotlinx.coroutines.launch
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import org.blocovermelho.mod.api.HttpError
import org.blocovermelho.mod.async.CoroutineManager
import org.blocovermelho.mod.ext.Helpers.err
import org.quiltmc.qkl.library.brigadier.util.player
import org.quiltmc.qkl.library.brigadier.util.sendFeedback
import org.quiltmc.qkl.library.text.*

fun CommandContext<ServerCommandSource>.sendError(err: HttpError,  _when: String) {
    player?.sendErr(err, _when)
}

fun CommandContext<ServerCommandSource>.sendAny(string: String) {
    sendFeedback {
        buildText { literal(string) }
    }
}

fun ServerPlayerEntity.sendErr(err: HttpError,  _when: String) {
    this.sendSystemMessage(buildText {
        err {
            literal("Um erro aconteceu durante:")
            color(Color.YELLOW) {
                literal(" $_when\n")
            }

            italic {
                literal("Erro: ")
                color(Color.LIGHT_PURPLE) {
                    literal(err.second.error)
                }
                if (err.second.inner != null) {
                    literal(": ${err.second.inner!!}")
                }

                literal("\nCódigo do erro: ${err.first.value}, ${err.first.description}")
            }

        }
    })
}
