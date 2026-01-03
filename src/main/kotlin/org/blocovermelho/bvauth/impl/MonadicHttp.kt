package org.blocovermelho.bvauth.impl

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.blocovermelho.bvauth.BvAuthMod

typealias HTTPReply<T> = Result<T, Pair<HttpStatusCode, String>>


suspend inline fun <reified T> m_run(request: (() -> HttpResponse)): HTTPReply<T> {
    val response = request()
    val code = response.status

    val result: HTTPReply<T> = if (!code.isSuccess()) {
        Err(Pair(code, response.bodyAsText()))
    } else {
        Ok(response.body<T>())
    }

    return result
}

suspend inline fun <reified T> HttpClient.m_get(path: String, builder: HttpRequestBuilder.() -> Unit): HTTPReply<T> {
    return m_run<T> {
        this.get(path, builder)
    }
}

suspend inline fun <reified T> HttpClient.m_post(path: String, builder: HttpRequestBuilder.() -> Unit): HTTPReply<T> {
    return m_run<T> {
        this.post(path, builder)
    }
}
