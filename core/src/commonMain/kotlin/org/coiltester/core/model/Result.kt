package org.coiltester.core.model

import kotlinx.serialization.Serializable

/**
 * Result type for typed error handling without exceptions.
 * Based on Kotlin Result but with explicit sealed hierarchy for serialization.
 */
@Serializable
sealed class Result<out T, out E> {
    @Serializable
    data class Ok<out T>(val value: T) : Result<T, Nothing>()

    @Serializable
    data class Err<out E>(val error: E) : Result<Nothing, E>()

    companion object {
        inline fun <T, E : Throwable> runCatching(block: () -> T): Result<T, E> {
            return try {
                Ok(block())
            } catch (e: Exception) {
                Err(e as E)
            }
        }
    }

    fun isOk(): Boolean = this is Ok<*>
    fun isErr(): Boolean = this is Err<*>

    fun getOrNull(): T? = when (this) {
        is Ok -> value
        is Err -> null
    }

    fun getOrThrow(): T = when (this) {
        is Ok -> value
        is Err -> throw error as Throwable
    }

    fun <R> map(transform: (T) -> R): Result<R, E> = when (this) {
        is Ok -> Ok(transform(value))
        is Err -> this as Result<R, E>
    }

    @Suppress("UNCHECKED_CAST")
    fun <R> flatMap(transform: (T) -> Result<R, @UnsafeVariance E>): Result<R, E> = when (this) {
        is Ok -> transform(value)
        is Err -> this as Result<R, E>
    }

    fun onOk(action: (T) -> Unit): Result<T, E> = when (this) {
        is Ok -> { action(value); this }
        is Err -> this
    }

    fun onErr(action: (E) -> Unit): Result<T, E> = when (this) {
        is Ok -> this
        is Err -> { action(error); this }
    }
}