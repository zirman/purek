@file:Suppress("unused")

package functional.stack

import functional.list.Link
import functional.list.LinkList
import functional.state.State

fun pop(): State<LinkList<Int>, Int> = { s ->
    s as Link
    Pair(s.head, s.tail)
}

fun push(x: Int): State<LinkList<Int>, Unit> = { s -> Pair(Unit, Link(x, s)) }

fun get(): State<LinkList<Int>, LinkList<Int>> = { s -> Pair(s, s) }

fun put(s: LinkList<Int>): State<LinkList<Int>, Unit> = { Pair(Unit, s) }
