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

object BVClient {
    lateinit var client: HttpClient
    lateinit var apiConfig: BVApiConfig

    lateinit var httpEndpoint: String
    lateinit var websocketEndpoint: String

    @ExperimentalSerializationApi
    fun init(config: BVApiConfig) {
        apiConfig = config
        httpEndpoint = if (apiConfig.tls) {
            "https://${apiConfig.endpoint}"
        } else {
            "http://${apiConfig.endpoint}"
        }
        websocketEndpoint = "ws://${apiConfig.endpoint}"

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
        bearerAuth(apiConfig.token)
    }
}

suspend inline fun <reified T, reified A> BVClient.Post(path: String, body: A? = null): HttpResult<T> {
    return client.m_post<T>(httpEndpoint + path) {
        bearerAuth(apiConfig.token)
        if (body != null) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }
}

suspend inline fun <reified T, reified A> BVClient.Patch(path: String, body: A? = null): HttpResult<T> {
    return client.m_patch<T>(httpEndpoint + path) {
        bearerAuth(apiConfig.token)
        if (body != null) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }
}

suspend inline fun <reified T> BVClient.Delete(path: String): HttpResult<T> {
    return client.m_delete<T>(httpEndpoint + path) {
        bearerAuth(apiConfig.token)
    }
}


@Serializable
data class BVApiConfig(val endpoint: String, val token: String, val tls: Boolean) {
    companion object {
        val Default = BVApiConfig("localhost:8080", "test", false)
    }
}
