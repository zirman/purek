@file:Suppress("unused")

package functional.diffList

import functional.identity
import functional.linkedList.Cell
import functional.linkedList.End
import functional.linkedList.LinkedList
import functional.linkedList.foldl

typealias DiffList<A> = (LinkedList<A>) -> LinkedList<A>

fun <A> emptyDiffList(): DiffList<A> = ::identity

fun <A> diffListOf(vararg s: A): DiffList<A> = { s.foldRight(it, ::Cell) }

fun <A> A.unitDiffList(): DiffList<A> = { tail -> Cell(this, tail) }

fun <A> DiffList<A>.append(xs: DiffList<A>): DiffList<A> = { tail -> invoke(xs(tail)) }

fun <A> DiffList<A>.prepend(xs: DiffList<A>): DiffList<A> = { tail -> xs(invoke(tail)) }

fun <A> DiffList<A>.build(): LinkedList<A> = invoke(End)

fun DiffList<String>.buildString(separator: CharSequence = ""): String =
    build().foldl(StringBuilder()) { acc, x -> acc.append(x).append(separator) }.toString()
