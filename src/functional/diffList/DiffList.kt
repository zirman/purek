@file:Suppress("unused")

package functional.diffList

import functional.list.Cell
import functional.list.LinkedList
import functional.list.End
import functional.list.foldl

typealias DiffList<A> = (LinkedList<A>) -> LinkedList<A>

fun <A> emptyDiffList(): DiffList<A> = { it }

fun <A> diffListOf(vararg s: A): DiffList<A> = { s.foldRight(it, ::Cell) }

fun <A> A.toDiffList(): DiffList<A> = { Cell(this, it) }

fun <A> DiffList<A>.append(xs: DiffList<A>): DiffList<A> = { this(xs(it)) }

fun <A> DiffList<A>.prepend(xs: DiffList<A>): DiffList<A> = { xs(this(it)) }

fun <A> DiffList<A>.build(): LinkedList<A> = this(End)

fun DiffList<String>.buildString(separator: CharSequence = ""): String =
    build().foldl(StringBuilder()) { acc, x -> acc.append(x).append(separator) }.toString()
