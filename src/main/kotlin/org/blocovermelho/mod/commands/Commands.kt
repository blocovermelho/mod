package org.blocovermelho.mod.commands

import org.blocovermelho.mod.BVQuilt
import org.blocovermelho.mod.commands.admin.AdminCommands
import org.quiltmc.qkl.library.EventRegistration
import org.quiltmc.qkl.library.brigadier.register
import org.quiltmc.qkl.library.commands.onCommandRegistration
import org.quiltmc.qkl.library.registerEvents
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer

fun registerCommands() {
    registerEvents {
        onCommandRegistration { _,_ ->
            LoginCommand.register(this)
            RegisterCommand.register(this)
            LinkCommand.register(this)
            ChangePasswordCommand.register(this)

            AdminCommands.register(this)

            BVQuilt.LOGGER.info("Registered commands.")
        }
    }
}
