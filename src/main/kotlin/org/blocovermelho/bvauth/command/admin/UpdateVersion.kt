package org.blocovermelho.bvauth.command.admin

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import kotlinx.coroutines.launch
import net.minecraft.SharedConstants
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import org.blocovermelho.bvauth.BvAuthMod
import org.blocovermelho.bvauth.api.routes.GameServer
import org.blocovermelho.bvauth.ext.Core.array
import org.blocovermelho.bvauth.ext.Headers
import org.blocovermelho.bvauth.impl.Err
import org.blocovermelho.bvauth.impl.Ok
import org.blocovermelho.bvauth.ext.divAssign
import org.blocovermelho.bvauth.ext.dsl.Color
import org.blocovermelho.bvauth.ext.dsl.buildLine
import org.blocovermelho.bvauth.ext.dsl.color
import org.blocovermelho.bvauth.ext.dsl.literal
import org.blocovermelho.bvauth.ext.dsl.plusAssign
import org.blocovermelho.bvauth.ext.message
import org.blocovermelho.bvauth.ext.unaryMinus
import org.blocovermelho.bvauth.ext.username
import org.blocovermelho.bvauth.impl.CoroutineManager

object UpdateVersion {
    fun register(
        dispatcher: CommandDispatcher<CommandSourceStack>
    ) {
        var root = Commands.literal("update-version").requires { !it.isPlayer
                || BvAuthMod.Config.Server.Staff.unaryMinus().contains(it.player?.username()) && BvAuthMod.LoggedUsers.contains(it.player?.uuid)
        }.executes {
            val version = SharedConstants.getCurrentVersion().name()
            if (version.isNotEmpty()) {
                CoroutineManager.scope.launch {
                    callApi(it, version)
                }
            } else {
                it.source.sendFailure(
                    buildLine(Headers.Server, Headers.Admin, buildLine {
                        this += "Não foi possivel descobrir a versão do jogo automáticamente. Adcione a versão manualmente e tente novamente."
                    })
                )
            }
            1
        }.then(Commands.argument("version", StringArgumentType.word()).executes {
            val version = StringArgumentType.getString(it, "version")
            if (version.isNotEmpty()) {
                CoroutineManager.scope.launch {
                    callApi(it, version)
                }
            } else {
                it.source.sendFailure(buildLine(Headers.Server, Headers.Admin, buildLine {
                    this += "A versão especificada está em branco. Digite a versão correta e tente novamente."
                }))
            }
            1
        })

        dispatcher.register(root)
    }

    suspend fun callApi(ctx: CommandContext<CommandSourceStack>,  version: String) {
        val update = GameServer.UpdateVersions(listOf(version))
        when (update) {
           is Ok -> {
               BvAuthMod.Config.Server.Versoes /= update.value.versions
               BvAuthMod.Config.save()

               ctx.source.sendSuccess({ buildLine(
                   buildLine {
                       this += "Versões atualizadas para"
                       this += {
                           array(update.value.versions){
                               color(Color.BLUE) {
                                   literal(it)
                               }
                           }
                       }
                   }
               )}, false)
           }
            is Err -> {
                ctx.source.sendFailure(buildLine (update.message("Atualizando a versão")))
            }
        }

    }
}