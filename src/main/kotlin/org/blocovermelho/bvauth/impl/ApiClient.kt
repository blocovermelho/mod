package org.blocovermelho.bvauth.impl

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import org.blocovermelho.bvauth.BvAuthMod
import org.blocovermelho.bvauth.config.ModConfig
import org.blocovermelho.bvauth.ext.unaryMinus

object ApiClient {
    lateinit var client: HttpClient
    lateinit var settings: ModConfig.ApiSettings

    fun init(settings: ModConfig.ApiSettings): ApiClient {
        this.settings = settings
        client = HttpClient(CIO) {
            install(Auth) {
                bearer {
                    loadTokens {
                        BearerTokens(-settings.ApiToken, "")
                    }
                }
            }

            install(ContentNegotiation) {
                json(BvAuthMod.Json)
            }

            install(WebSockets) {
                pingIntervalMillis = 15_000
            }
        }

        return this
    }


    suspend inline fun <reified T> Get(path: String): HTTPReply<T> {
        return client.m_get<T>(
            if (-settings.TLS) {
                "https://"
            } else {
                "http://"
            } + (-settings.Endpoint) + path
        ) {

        }
    }

    suspend inline fun <reified T, reified A> Post(path: String, body: A? = null): HTTPReply<T> {
        return client.m_post<T>(
            if (-settings.TLS) {
                "https://"
            } else {
                "http://"
            } + (-settings.Endpoint) + path
        ) {
            if (body != null) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }
    }
}

