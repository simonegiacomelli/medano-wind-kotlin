package me.simone.application

import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals


class ServerKtTest {
    @Test
    fun parse() {
        val target = parseDate("Date(2023, 0, 27, 12, 35)")
        assertEquals(LocalDateTime(2023, 1, 27, 12, 35), target)
    }
}