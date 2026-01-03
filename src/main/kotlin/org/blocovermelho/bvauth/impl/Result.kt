package org.blocovermelho.bvauth.impl

open class Result<T, E>

class Ok<T, E>(val value: T) : Result<T, E>()
class Err<T, E>(val error: E) : Result<T, E>()

fun <T, E> Result<T, E>.expect(f: (E) -> String): T {
    return when (this) {
        is Ok -> value
        is Err -> throw Error(f(error))
        else -> throw UnsupportedOperationException()
    }
}

fun <T, E, R> Result<T, E>.map(f: (T) -> R): Result<R, E> {
    return when (this) {
        is Ok -> Ok(f(value))
        is Err -> Err(error)
        else -> throw UnsupportedOperationException()
    }
}

fun <T, E> Result<T, E>.unwrap_or(default: T): T {
    return when (this) {
        is Ok -> value
        is Err -> default
        else -> throw UnsupportedOperationException()
    }
}

fun <T, E> Result<T, E>.unwrap_or_else(op: () -> T): T {
    return when (this) {
        is Ok -> value
        is Err -> op()
        else -> throw UnsupportedOperationException()
    }
}

fun <T, E, U> Result<T, E>.and(res: Result<U, E>): Result<U, E> {
    return when (this) {
        is Ok -> res
        is Err -> Err(error)
        else -> throw UnsupportedOperationException()
    }
}

fun <T, E, U> Result<T, E>.and_then(op: (T) -> Result<U, E>): Result<U, E> {
    return when (this) {
        is Ok -> op(value)
        is Err -> Err(error)
        else -> throw UnsupportedOperationException()
    }
}

fun <T, E, R> Result<T, E>.or(res: Result<T, R>): Result<T, R> {
    return when (this) {
        is Ok -> Ok(value)
        is Err -> res
        else -> throw UnsupportedOperationException()
    }
}

fun <T, E, R> Result<T, E>.or_else(op: () -> Result<T, R>): Result<T, R> {
    return when (this) {
        is Ok -> Ok(value)
        is Err -> op()
        else -> throw UnsupportedOperationException()
    }
}

fun <T, E> Result<T, E>.ok(): T? {
    return when (this) {
        is Ok -> value
        is Err -> null
        else -> throw UnsupportedOperationException()
    }
}

fun <T, E> Result<T?, E>.transpose(): Result<T, E>? {
    return when (this) {
        is Ok -> {
            if (value != null) {
                Ok(value)
            } else {
                null
            }
        }

        is Err -> Err(error)
        else -> throw UnsupportedOperationException()
    }
}