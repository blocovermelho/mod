package org.blocovermelho.mod.ext

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.server.command.ServerCommandSource

typealias Subcommand = LiteralArgumentBuilder<ServerCommandSource>;
