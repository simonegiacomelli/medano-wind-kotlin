import kotlinx.browser.document

fun main() {
    val container = document.createElement("div")
    document.body!!.appendChild(container)
}