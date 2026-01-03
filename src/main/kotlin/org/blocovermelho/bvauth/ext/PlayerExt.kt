package org.blocovermelho.bvauth.ext

import net.minecraft.server.level.ServerPlayer


fun ServerPlayer.username() : String {
    return this.gameProfile.name
}