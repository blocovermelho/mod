package org.blocovermelho.mod.events

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.blocovermelho.mod.async.CoroutineManager
import org.blocovermelho.mod.ext.launch
import org.blocovermelho.mod.service.PlayerLinkingService
import org.blocovermelho.mod.service.WebsocketService
import org.quiltmc.qkl.library.lifecycle.onServerReady
import org.quiltmc.qkl.library.lifecycle.onServerStarting
import org.quiltmc.qkl.library.lifecycle.onServerStopping
import org.quiltmc.qkl.library.registerEvents

fun onServerInit() {
    registerEvents {
        onServerStarting {
            CoroutineManager.init()
        }
    }
}

fun onServerReady(){
    registerEvents {
        onServerReady {
            launch {
                val mc = this
                coroutineScope {
                    launch { WebsocketService.launch() }
                    launch { PlayerLinkingService.launch(mc) }
                    launch { createNewServer() }
                }
            }
        }

        onServerStopping {
            launch { disableServer() }
        }
    }
}

