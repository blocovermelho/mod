package org.blocovermelho.mod.api

open class Result<T, E>

class Ok<T, E>(val value: T) : Result<T, E>()
class Err<T, E>(val error: E) : Result<T, E>()

fun <T, E> Result<T, E>.handleErr(f: (E) -> Unit): T? {
    return when (this) {
        is Ok -> this.value
        is Err -> {
            f(this.error)
            null
        }

        else -> null
    }
}
