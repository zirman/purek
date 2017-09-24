@file:Suppress("unused")

package functional.stack

import functional.list.Cell
import functional.list.LinkedList
import functional.state.State

fun pop(): State<LinkedList<Int>, Int> = State { s ->
    s as Cell
    Pair(s.head, s.tail)
}

fun push(x: Int): State<LinkedList<Int>, Unit> = State { s -> Pair(Unit, Cell(x, s)) }

fun get(): State<LinkedList<Int>, LinkedList<Int>> = State { s -> Pair(s, s) }

fun put(s: LinkedList<Int>): State<LinkedList<Int>, Unit> = State { Pair(Unit, s) }
