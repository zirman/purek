@file:Suppress("unused")

package functional.zipper

import functional.linkedList.Cell
import functional.linkedList.LinkedList

sealed class Tree<out A>
data class Node<out A>(val x: A, val l: Tree<A>, val r: Tree<A>) : Tree<A>()
object Empty : Tree<Nothing>()

sealed class Crumb<out A>
data class LeftCrumb<out A>(val x: A, val r: Tree<A>) : Crumb<A>()
data class RightCrumb<out A>(val x: A, val l: Tree<A>) : Crumb<A>()

data class Zipper<out A>(val t: Tree<A>, val bs: LinkedList<Crumb<A>>)

fun <A> Zipper<A>.goLeft(): Zipper<A> {
    t as Node
    val (x, l, r) = t
    return Zipper(l, Cell(LeftCrumb(x, r), bs))
}

fun <A> Zipper<A>.goRight(): Zipper<A> {
    t as Node
    val (x, l, r) = t
    return Zipper(r, Cell(RightCrumb(x, l), bs))
}

fun <A> Zipper<A>.goUp(): Zipper<A> {
    bs as Cell
    val (b, bs2) = bs

    return when (b) {
        is LeftCrumb -> Zipper(Node(b.x, t, b.r), bs2)
        is RightCrumb -> Zipper(Node(b.x, b.l, t), bs2)
    }
}

fun <A> Zipper<A>.modify(f: (A) -> A): Zipper<A> = when (t) {
    is Node -> Zipper(Node(f(t.x), t.l, t.r), bs)
    else -> this
}

fun <A> Zipper<A>.attach(t: Tree<A>): Zipper<A> = Zipper(t, bs)

fun <A> Zipper<A>.topMost(): Zipper<A> = when (bs) {
    is Cell -> goUp().topMost()
    else -> this
}
