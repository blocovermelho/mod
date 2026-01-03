package org.blocovermelho.bvauth.api.routes


import org.blocovermelho.bvauth.api.types.WebSocketMessage
import org.blocovermelho.bvauth.ext.UrlEncode
import org.blocovermelho.bvauth.impl.ApiClient
import org.blocovermelho.bvauth.impl.expect

object Discord {
    const val BASE_PATH = "/link"
    suspend fun GetNewLink(username: String) =
        ApiClient.Get<String>("$BASE_PATH/new?username=$username")
            .expect { "This API is infallible." }

    suspend fun ManualLink(username: String, token: String) =
        ApiClient.Get<WebSocketMessage.DiscordLink>("$BASE_PATH/manual?username=$username&token=${token.UrlEncode()}")
}