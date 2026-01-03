package org.blocovermelho.bvauth.api.routes

import org.blocovermelho.bvauth.api.types.KeepAlive
import org.blocovermelho.bvauth.api.types.Server
import org.blocovermelho.bvauth.impl.ApiClient


object GameServer {
    const val BASE_PATH = "/server"
    suspend fun KeepAlive(packet: KeepAlive?) =
        ApiClient.Post<String, KeepAlive>("$BASE_PATH/@me/heartbeat", packet)

    suspend fun Me() =
        ApiClient.Get<Server>("$BASE_PATH/@me")
}