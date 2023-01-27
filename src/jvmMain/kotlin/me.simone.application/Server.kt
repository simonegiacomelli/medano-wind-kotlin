@file:OptIn(ExperimentalTime::class)

package me.simone.application

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.net.URL
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

@Serializable
data class Row(val date: LocalDateTime, val wind: Int, val gusts: Int)

fun main() {
    val windDb = WindDb("wind.json")
    windDb.fetch(19000)
    val elements = windDb.rows()
    println("${elements.size} elements")
    elements.groupBy { it.date.year }.map {
        println("${it.key} ${it.value.size}")
    }
}

fun parseDate(content: String): LocalDateTime {
    val (y, mo, d, h, mi) = content.removePrefix("Date(").removeSuffix(")")
        .split(",").map { it.trim().toInt() }
    return LocalDateTime(y, mo + 1, d, h, mi)
}

class WindDb(val file: File) {
    constructor(file: String) : this(File(file))

    fun content(): String {
        if (file.exists())
            return file.readText()
        return fetch()
    }

    fun fetch(hours: Int = 17000): String {
        val url = "https://cabezo.bergfex.at/wetterstation/data/json.php?hours=$hours"
        println(url)
        val str = URL(url).openStream().use { it.bufferedReader().readText() }
        file.writeText(str)
        return str
    }

    fun rows(): List<Row> = rowsFromUntyped(content())

    private fun rowsFromUntyped(string: String) = Json.parseToJsonElement(string).jsonArray.map { r ->
        val (a, b, c, _) = r.jsonArray.map { it.jsonPrimitive }
        Row(parseDate(a.content), b.int, c.int)
    }
}