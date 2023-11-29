package org.blocovermelho.mod.events

import org.blocovermelho.mod.ext.launch
import org.quiltmc.qkl.library.networking.onPlayDisconnect
import org.quiltmc.qkl.library.networking.onPlayInit
import org.quiltmc.qkl.library.registerEvents

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

