package org.coiltester.core.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName

class ResultTest {

    @Test
    @DisplayName("Ok should hold value")
    fun okHoldsValue() {
        val result: Result<Int, String> = Result.Ok(42)
        assertTrue(result.isOk())
        assertFalse(result.isErr())
        assertEquals(42, result.getOrNull())
        assertEquals(42, result.getOrThrow())
    }

    @Test
    @DisplayName("Err should hold error")
    fun errHoldsError() {
        val result: Result<Int, String> = Result.Err("error")
        assertFalse(result.isOk())
        assertTrue(result.isErr())
        assertNull(result.getOrNull())
    }

    @Test
    @DisplayName("map should transform Ok value")
    fun mapTransformsOk() {
        val result: Result<Int, String> = Result.Ok(42)
        val mapped = result.map { it * 2 }
        assertEquals(84, mapped.getOrNull())
    }

    @Test
    @DisplayName("map should preserve Err")
    fun mapPreservesErr() {
        val result: Result<Int, String> = Result.Err("error")
        val mapped = result.map { it * 2 }
        assertTrue(mapped.isErr())
    }

    @Test
    @DisplayName("flatMap should chain operations")
    fun flatMapChains() {
        val result: Result<Int, String> = Result.Ok(42)
        val chained = result.flatMap { Result.Ok(it * 2) }
        assertEquals(84, chained.getOrNull())
    }

    @Test
    @DisplayName("flatMap should short-circuit on Err")
    fun flatMapShortCircuits() {
        val result: Result<Int, String> = Result.Err("error")
        val chained = result.flatMap { Result.Ok(it * 2) }
        assertTrue(chained.isErr())
    }

    @Test
    @DisplayName("onOk should execute on Ok")
    fun onOkExecutes() {
        var executed = false
        val result: Result<Int, String> = Result.Ok(42)
        result.onOk { executed = true }
        assertTrue(executed)
    }

    @Test
    @DisplayName("onErr should execute on Err")
    fun onErrExecutes() {
        var executed = false
        val result: Result<Int, String> = Result.Err("error")
        result.onErr { executed = true }
        assertTrue(executed)
    }

}