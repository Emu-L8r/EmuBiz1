package com.example.databaser.data

import org.junit.Test
import kotlin.test.assertEquals

class ConvertersTest {
    @Test
    fun testListSerializationRoundTrip() {
        val list = listOf("one", "two,with,comma", "three")
        val json = Converters.fromList(list)
        val parsed = Converters.fromString(json)
        assertEquals(list, parsed)
    }

    @Test
    fun testEmptyList() {
        val list = emptyList<String>()
        val json = Converters.fromList(list)
        val parsed = Converters.fromString(json)
        assertEquals(list, parsed)
    }
}

