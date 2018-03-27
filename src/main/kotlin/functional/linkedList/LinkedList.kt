@file:Suppress("unused")

package functional.linkedList

import functional.either.Either
import functional.either.Right
import functional.either.bind
import functional.flip
import functional.option.Just
import functional.option.Option
import functional.option.bind

sealed class LinkedList<out A>

data class Cell<out A>(val head: A, val tail: LinkedList<A>) : LinkedList<A>() {
    override fun toString(): String = (tail.buildString(
        StringBuilder()
            .append("[")
            .append(head)
    ))
        .append("]")
        .toString()
}

object End : LinkedList<Nothing>() {
    override fun toString(): String = "[]"
}

fun <A> linkedListOf(vararg s: A): LinkedList<A> = s.foldRight(End, ::Cell)

tailrec fun <A> LinkedList<A>.buildString(stringBuilder: StringBuilder): StringBuilder =
    when (this) {
        is Cell -> tail.buildString(stringBuilder.append(", ").append(head))
        else -> stringBuilder
    }

fun <A> LinkedList<A>.filter(p: (A) -> Boolean): LinkedList<A> = when (this) {
    is Cell -> if (p(head)) Cell(head, tail.filter(p)) else tail.filter(p)
    else -> End
}

tailrec fun <A, B> LinkedList<A>.foldl(acc: B, f: (B, A) -> B): B = when (this) {
    is Cell -> tail.foldl(f(acc, head), f)
    else -> acc
}

fun <A, B> LinkedList<A>.foldr(acc: B, f: (A, B) -> B): B = when (this) {
    is Cell -> f(head, tail.foldr(acc, f))
    else -> acc
}

fun <A> Cell<A>.reduce(f: (A, A) -> A): A = foldl(head, f)

fun <A> LinkedList<A>.reverse(): LinkedList<A> = foldl(End, flip(::Cell))

fun <A> LinkedList<A>.concat(to: LinkedList<A>): LinkedList<A> = foldr(to, ::Cell)

fun <A> LinkedList<A>.concatStackSafe(to: LinkedList<A>): LinkedList<A> =
    reverse().foldl(to) { tail, head -> Cell(head, tail) }

fun <A, B> LinkedList<A>.flatMap(f: (A) -> LinkedList<B>): LinkedList<B> = when (this) {
    is Cell -> f(head).concat(tail.bind(f))
    else -> End
}

fun <A> LinkedList<A>.filterStackSafe(p: (A) -> Boolean): LinkedList<A> =
    reverse().foldl(End as LinkedList<A>) { tail, head ->
        if (p(head)) Cell(head, tail.filter(p)) else tail.filter(p)
    }

inline fun <A, B> LinkedList<A>.foldrStackSafe(acc1: B, crossinline f: (A, B) -> B): B =
    reverse().foldl(acc1) { acc2, head -> f(head, acc2) }

// list functor function

fun <A, B> LinkedList<A>.fmap(f: (A) -> B): LinkedList<B> = when (this) {
    is Cell -> Cell(f(head), tail.fmap(f))
    else -> End
}

inline fun <A, B> LinkedList<A>.fmapStackSafe(crossinline f: (A) -> B): LinkedList<B> =
    reverse().foldl(End as LinkedList<B>) { tail, head -> Cell(f(head), tail) }

// list applicative functions

fun <A> A.unitLinkedList(): LinkedList<A> = Cell(this, End)

fun <A, B> LinkedList<(A) -> B>.ap(av: LinkedList<A>): LinkedList<B> =
    foldr(End as LinkedList<B>) { f, a -> av.foldr(a) { x, a1 -> Cell(f(x), a1) } }

fun <A, B> LinkedList<A>.apRight(r: LinkedList<B>): LinkedList<B> =
    foldr(End as LinkedList<B>) { _, a -> r.foldr(a) { x, a1 -> Cell(x, a1) } }

fun <A, B> LinkedList<A>.apLeft(r: LinkedList<B>): LinkedList<A> =
    foldr(End as LinkedList<A>) { x, a -> r.foldr(a) { _, a1 -> Cell(x, a1) } }

// list monad functions

fun <A> LinkedList<LinkedList<A>>.join(): LinkedList<A> =
    foldr(End as LinkedList<A>) { x, acc -> x.foldr(acc, ::Cell) }

inline fun <A, B> LinkedList<A>.bind(crossinline f: (A) -> LinkedList<B>): LinkedList<B> =
    foldr(End as LinkedList<B>) { x, acc -> f(x).foldr(acc, ::Cell) }

inline fun <A, B, C> ((A) -> LinkedList<B>).fish(crossinline f: (B) -> LinkedList<C>): (A) -> LinkedList<C> =
    { x -> invoke(x).bind(f) }

fun <A, B : Any> LinkedList<A>.foldM(acc1: B, f: (B, A) -> Option<B>): Option<B> =
    when (this) {
        is Cell -> f(acc1, head).bind { acc2 -> tail.foldM(acc2, f) }
        else -> Just(acc1)
    }

fun <A, B, C> LinkedList<A>.foldM(acc1: B, f: (B, A) -> Either<C, B>): Either<C, B> =
    when (this) {
        is Cell -> f(acc1, head).bind { acc2 -> tail.foldM(acc2, f) }
        else -> Right(acc1)
    }
