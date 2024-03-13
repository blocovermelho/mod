package org.blocovermelho.mod.events

import kotlinx.coroutines.runBlocking
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import org.blocovermelho.mod.events.qkl.onPreLogin
import org.blocovermelho.mod.ext.launch
import org.quiltmc.qkl.library.networking.onPlayDisconnect
import org.quiltmc.qkl.library.networking.onPlayInit
import org.quiltmc.qkl.library.registerEvents

fun onLoginAttempt() {
    registerEvents {
        onPreLogin { server, address, profile ->
            runBlocking {
                onLoginAttempt(server, address, profile);
            }
        }
    }
}

fun onPlayerJoin() {
    registerEvents {
        onPlayInit {
            launch {
                checkSession(player)
                sendServerDetails(player)
                sendAwkMessage(player)
            }
        }
    }
}

fun onPlayerLeft() {
    registerEvents {
        onPlayDisconnect {
            launch {
                handleDisconnect(player)
            }
        }
    }
}

