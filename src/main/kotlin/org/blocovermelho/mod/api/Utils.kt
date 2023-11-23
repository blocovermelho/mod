package org.blocovermelho.mod.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

typealias HttpResult<T> = Result<T, Pair<HttpStatusCode, HttpFailure>>

@Serializable
data class HttpFailure(
    val error: String,
    val inner: String?
)

suspend inline fun <reified T> m_run(request: (() -> HttpResponse)): HttpResult<T> {
    val response = request()
    val code = response.status

    val result: HttpResult<T> = if (!code.isSuccess()) {
        Err(Pair(code, response.body<HttpFailure>()))
    } else {
        Ok(response.body<T>())
    }

    return result
}


suspend inline fun <reified T> HttpClient.m_get(path: String, builder: HttpRequestBuilder.() -> Unit): HttpResult<T> {
    return m_run {
        this.get(path, builder)
    }
}

suspend inline fun <reified T> HttpClient.m_delete(
    path: String,
    builder: HttpRequestBuilder.() -> Unit
): HttpResult<T> {
    return m_run {
        this.delete(path, builder)
    }
}

suspend inline fun <reified T> HttpClient.m_post(path: String, builder: HttpRequestBuilder.() -> Unit): HttpResult<T> {
    return m_run {
        this.post(path, builder)
    }
}

suspend inline fun <reified T> HttpClient.m_patch(path: String, builder: HttpRequestBuilder.() -> Unit): HttpResult<T> {
    return m_run {
        this.patch(path, builder)
    }
}
