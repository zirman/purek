@file:Suppress("unused")

package functional.stack

import functional.linkedList.Cell
import functional.linkedList.LinkedList
import functional.state.State

fun pop(): State<LinkedList<Int>, Int> = { s -> s as Cell; Pair(s.tail, s.head) }
fun push(x: Int): State<LinkedList<Int>, Unit> = { s -> Pair(Cell(x, s), Unit) }
fun get(): State<LinkedList<Int>, LinkedList<Int>> = { s -> Pair(s, s) }
fun put(s: LinkedList<Int>): State<LinkedList<Int>, Unit> = { _ -> Pair(s, Unit) }
