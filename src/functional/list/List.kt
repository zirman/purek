@file:Suppress("unused")

package functional.list

import functional.either.Either
import functional.either.right
import functional.maybe.just
import functional.maybe.Maybe
import functional.maybe.bind
import functional.path.bind

sealed class LinkedList<out A>

data class Cell<out A> internal constructor(val head: A, val tail: LinkedList<A>) : LinkedList<A>() {
    override fun toString(): String = (tail.buildString(StringBuilder()
        .append("[")
        .append(head)))
        .append("]")
        .toString()
}

object End : LinkedList<Nothing>() {
    override fun toString(): String = "[]"
}

fun <A> linkedListOf(vararg s: A): LinkedList<A> = s.foldRight(End, ::Cell)

fun <T> LinkedList<T>.buildString(stringBuilder: StringBuilder): StringBuilder = when (this) {
    End -> stringBuilder
    is Cell -> tail.buildString(stringBuilder.append(", ").append(head))
}

fun <T> LinkedList<T>.filter(p: (T) -> Boolean): LinkedList<T> = when (this) {
    End -> End
    is Cell -> if (p(head)) Cell(head, tail.filter(p)) else tail.filter(p)
}

fun <T, U> LinkedList<T>.foldl(acc: U, f: (U, T) -> U): U = when (this) {
    End -> acc
    is Cell -> tail.foldl(f(acc, head), f)
}

fun <T, U> LinkedList<T>.foldr(acc: U, f: (T, U) -> U): U = when (this) {
    End -> acc
    is Cell -> f(head, tail.foldr(acc, f))
}

fun <T> Cell<T>.reduce(f: (T, T) -> T): T = foldl(head, f)

fun <T> LinkedList<T>.reverse(): LinkedList<T> = foldr(End, ::Cell)

fun <T> LinkedList<T>.concat(to: LinkedList<T>): LinkedList<T> = foldr(to, ::Cell)

fun <T, U> LinkedList<T>.flatMap(f: (T) -> LinkedList<U>): LinkedList<U> = when (this) {
    End -> End
    is Cell -> f(head).concat(tail.bind(f))
}

fun <T, U> LinkedList<T>.mapStackSafe(f: (T) -> U): LinkedList<U> =
    reverse().foldl<T, LinkedList<U>>(End) { tail, head -> Cell(f(head), tail) }

fun <T> LinkedList<T>.filterStackSafe(p: (T) -> Boolean): LinkedList<T> =
    reverse().foldl<T, LinkedList<T>>(End) { tail, head ->
        if (p(head)) Cell(head, tail.filter(p)) else tail.filter(p)
    }

fun <T, U> LinkedList<T>.foldlStackSafe(f: (U, T) -> U, a: U): U {
    var acc = a
    var cell = this

    while (cell is Cell) {
        acc = f(acc, cell.head)
        cell = cell.tail
    }

    return acc
}

fun <T, U> LinkedList<T>.foldrNotRec(f: (T, U) -> U, acc1: U): U =
    reverse().foldlStackSafe({ acc2, head -> f(head, acc2) }, acc1)

fun <T> Cell<T>.reduceStackSafe(f: (T, T) -> T): T = foldlStackSafe(f, head)

fun <T> LinkedList<T>.reverseStackSafe(): LinkedList<T> =
    foldlStackSafe<T, LinkedList<T>>({ tail, head -> Cell(head, tail) }, End)

fun <T> LinkedList<T>.concatStackSafe(to: LinkedList<T>): LinkedList<T> =
    reverse().foldlStackSafe({ tail, head -> Cell(head, tail) }, to)

fun <T, U> LinkedList<T>.flatMapStackSafe(f: (T) -> LinkedList<U>): LinkedList<U> = when (this) {
    End -> End
    is Cell -> f(head).concatStackSafe(tail.flatMapStackSafe(f))
}

// list functor function

fun <A, B> LinkedList<A>.fmap(f: (A) -> B): LinkedList<B> = when (this) {
    End -> End
    is Cell -> Cell(f(head), tail.fmap(f))
}

// list applicative functions

fun <A> A.toLinkList(): LinkedList<A> = Cell(this, End)

fun <A, B> LinkedList<(A) -> B>.ap(av: LinkedList<A>): LinkedList<B> =
    foldr<(A) -> B, LinkedList<B>>(End) { f, a1 -> av.foldr(a1) { v, a2 -> Cell(f(v), a2) } }

// list monad functions

fun <A> LinkedList<LinkedList<A>>.join(): LinkedList<A> =
    foldr<LinkedList<A>, LinkedList<A>>(End) { x, acc -> x.foldr(acc, ::Cell) }

fun <A, B> LinkedList<A>.bind(f: (A) -> LinkedList<B>): LinkedList<B> =
    foldr<A, LinkedList<B>>(End) { x, acc -> f(x).foldr(acc, ::Cell) }

//fun <A, B> LinkedList<A>.bind2(f: (A) -> LinkedList<B>): LinkedList<B> = f.toLinkList().ap(this).join()

//fun <A, B> LinkedList<A>.bind3(f: (A) -> LinkedList<B>): LinkedList<B> = fmap(f).join()

fun <A, B, C> ((A) -> LinkedList<B>).fish(f: (B) -> LinkedList<C>): (A) -> LinkedList<C> =
    { x -> this(x).fmap(f).join() }

fun <A, B : Any> LinkedList<A>.foldM(acc1: B, f: (B, A) -> Maybe<B>): Maybe<B> =
    when (this) {
        End -> just(acc1)
        is Cell -> f(acc1, head).bind { acc2 -> tail.foldM(acc2, f) }
    }

fun <A, B, C> LinkedList<A>.foldM(acc1: B, f: (B, A) -> Either<C, B>): Either<C, B> =
    when (this) {
        End -> right(acc1)
        is Cell -> f(acc1, head).bind { acc2 -> tail.foldM(acc2, f) }
    }
