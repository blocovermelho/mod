package org.blocovermelho.mod.ext

import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import kotlinx.coroutines.launch
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayNetworkHandler
import org.blocovermelho.mod.async.CoroutineManager
import org.quiltmc.qkl.library.EventRegistration
import org.quiltmc.qkl.library.brigadier.execute
import org.quiltmc.qkl.library.networking.GenericPlayCallback

typealias SuspendCommandAction<S> = suspend CommandContext<S>.() -> Unit
typealias SuspendSPNH = suspend ServerPlayNetworkHandler.() -> Unit
typealias SuspendMS = suspend MinecraftServer.() -> Unit

fun <S> ArgumentBuilder<S, *>.launch(command: SuspendCommandAction<S>) {
    execute {
        CoroutineManager.scope.launch {
            command(this@execute)
        }
    }
}

fun ServerPlayNetworkHandler.launch(block: SuspendSPNH) {
    CoroutineManager.scope.launch {
        block()
    }
}


fun MinecraftServer.launch(block: SuspendMS) {
    CoroutineManager.scope.launch {
        block()
    }
}

