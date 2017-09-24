@file:Suppress("unused")

package functional

@DslMarker
annotation class HtmlMarker

@HtmlMarker
open class Tag(private val name: String) {
    private val children = mutableListOf<Tag>()

    protected fun <T : Tag> doInit(child: T, init: T.() -> Unit) {
        child.init()
        children.add(child)
    }

    override fun toString() = "<$name>${children.joinToString("")}</$name>"
}

fun table(init: TABLE.() -> Unit): TABLE = TABLE().apply(init)

class HTML(val classes: String) : Tag("html") {
    fun div(classes: String = "", init: DIV.() -> Unit) = doInit(DIV(classes), init)
}

class DIV(val classes: String) : Tag("div") {
    fun button(classes: String = "", init: BUTTON.() -> Unit) = doInit(BUTTON(classes), init)
    fun ul(classes: String = "", init: UL.() -> Unit) = doInit(UL(classes), init)
}

class BUTTON(val classes: String) : Tag("div") {
    fun span(classes: String = "", init: SPAN.() -> Unit = {}) = doInit(SPAN(classes), init)
    operator fun String.unaryPlus() {}
}

class SPAN(val classes: String) : Tag("span")

class UL(val classes: String) : Tag("ul") {
    fun li(init: LI.() -> Unit) = doInit(LI(), init)
}

class LI(var role: String = "", var classes: Set<String> = emptySet()) : Tag("li") {
    fun a(@Suppress("UNUSED_PARAMETER") tag: String = "", init: A.() -> Unit) = doInit(A(), init)
    operator fun String.unaryPlus() {}
}

class A : Tag("a") {
    operator fun String.unaryPlus() {}
}

class TABLE : Tag("table") {
    fun tr(init: TR.() -> Unit) = doInit(TR(), init)
}

class TR : Tag("tr") {
    fun td(init: TD.() -> Unit) = doInit(TD(), init)
}

class TD : Tag("td")

fun createTable() = table { tr { td {} } }

fun createHTML(): HTML = HTML("")

fun buildDropdown() = createHTML().div(classes = "dropdown") {
    button(classes = "btn dropdown-toggle") {
        +"Dropdown"
        span(classes = "caret")
    }
    ul(classes = "dropdown-menu") {
        li { a("#") { +"Action" } }
        li { a("#") { +"Action action" } }
        li { role = "separator"; classes = setOf("divider") }
        li { classes = setOf("divider-dropdown-header"); +"Header" }
    }
}

fun UL.item(href: String, name: String) = li { a(href) { +name } }
fun UL.divider() = li { role = "separator"; classes = setOf("divider") }
fun UL.dropdownHeader(text: String) = li { classes = setOf("dropdown-header"); +text }

fun buildDropdown2() = createHTML().div(classes = "dropdown") {
    button(classes = "btn dropdown-toggle") {
        +"Dropdown"
        span(classes = "caret")
    }
    ul(classes = "dropdown-menu") {
        item("#", "Action")
        item("#", "Action action")
        divider()
        dropdownHeader("Header")
    }
}
