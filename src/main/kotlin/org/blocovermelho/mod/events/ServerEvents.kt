package org.blocovermelho.mod.events

import org.blocovermelho.mod.BVQuilt
import org.blocovermelho.mod.api.Routes
import org.blocovermelho.mod.api.handleErr
import org.blocovermelho.mod.api.models.CreateServer
import org.blocovermelho.mod.api.models.Modpack
import org.blocovermelho.mod.async.CoroutineManager
import org.blocovermelho.mod.config.ServerDetails.ModpackSection
import org.blocovermelho.mod.ext.launch
import org.quiltmc.qkl.library.lifecycle.onServerReady
import org.quiltmc.qkl.library.lifecycle.onServerStarting
import org.quiltmc.qkl.library.networking.onLoginInit
import org.quiltmc.qkl.library.networking.onPlayDisconnect
import org.quiltmc.qkl.library.networking.onPlayInit
import org.quiltmc.qkl.library.networking.onPlayReady
import org.quiltmc.qkl.library.registerEvents
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer
import java.util.*

fun onServerInit() {
    registerEvents {
        onServerStarting {
            CoroutineManager.init(this)
        }
    }
}

fun onServerReady(){
    registerEvents {
        onServerReady {
            launch {
                createNewServer()
            }
        }
    }
}

