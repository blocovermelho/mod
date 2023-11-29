package org.blocovermelho.mod.api

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import org.blocovermelho.mod.config.ApiSettings

object BVClient {
    lateinit var client: HttpClient
    lateinit var apiConfig: ApiSettings

    lateinit var httpEndpoint: String
    lateinit var websocketEndpoint: String

    @ExperimentalSerializationApi
    fun init(config: ApiSettings) {
        apiConfig = config
        httpEndpoint = if (apiConfig.tls.value()) {
            "https://${apiConfig.endpoint.value()}"
        } else {
            "http://${apiConfig.endpoint.value()}"
        }
        websocketEndpoint = "ws://${apiConfig.endpoint.value()}"

        client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    namingStrategy = JsonNamingStrategy.SnakeCase
                    prettyPrint = true
                })
            }
        }
    }

}

suspend inline fun <reified T> BVClient.Get(path: String): HttpResult<T> {
    return client.m_get<T>(httpEndpoint + path) {
        bearerAuth(apiConfig.token.value())
    }
}

suspend inline fun <reified T, reified A> BVClient.Post(path: String, body: A? = null): HttpResult<T> {
    return client.m_post<T>(httpEndpoint + path) {
        bearerAuth(apiConfig.token.value())
        if (body != null) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }
}

suspend inline fun <reified T, reified A> BVClient.Patch(path: String, body: A? = null): HttpResult<T> {
    return client.m_patch<T>(httpEndpoint + path) {
        bearerAuth(apiConfig.token.value())
        if (body != null) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }
}

suspend inline fun <reified T> BVClient.Delete(path: String): HttpResult<T> {
    return client.m_delete<T>(httpEndpoint + path) {
        bearerAuth(apiConfig.token.value())
    }
}


@Serializable
data class BVApiConfig(val endpoint: String, val token: String, val tls: Boolean) {
    companion object {
        val Default = BVApiConfig("localhost:8080", "test", false)
    }
}
