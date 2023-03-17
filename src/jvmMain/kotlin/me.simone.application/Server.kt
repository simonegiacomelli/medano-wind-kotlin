@file:OptIn(ExperimentalTime::class)

package me.simone.application

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File
import java.net.URL
import kotlin.math.roundToInt
import kotlin.time.ExperimentalTime


private val LocalDateTime.hour_pad: String get() = "$hour".padStart(2, '0')

@Serializable
data class Row(val date: LocalDateTime, val wind: Int, val gusts: Int)

fun main() {
    val windDb = WindDb("wind.json")
    val elements = windDb.rows()
    println("${elements.size} elements")
    val limit = LocalDateTime(2023, 1, 15, 0, 0)
    elements
        .filter { it.date > limit }
//        .filter { it.date.hour in 9..17 }
        .groupBy { it.date.run { Pair(it.date.dayOfMonth, it.date.hour) } }
        .map {
            val dayOfMont = it.key.first
            val hour = it.key.second
            val windAverage = it.value.map { it.wind }.average().roundToInt()
            val gustsAverage = it.value.map { it.gusts }.average().roundToInt()
            "$dayOfMont $hour $windAverage $gustsAverage".split(" ").joinToString("\t")
        }.joinToString("\n")
        .let { "dayOfMont hour windAverage gustsAverage".split(" ").joinToString("\t") + "\n" + it }
        .also {
            copyToClipboard(it)
            println(it)
            File("out.tsv").writeText(it)
        }
}

fun copyToClipboard(string: String) {
    Toolkit.getDefaultToolkit()
        .systemClipboard
        .setContents(StringSelection(string), null)
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