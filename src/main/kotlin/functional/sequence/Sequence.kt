@file:Suppress("unused")

package functional.sequence

import functional.either.Either
import functional.either.Left
import functional.either.Right
import functional.option.Just
import functional.option.None
import functional.option.Option

// sequence applicative functions

fun <A> A.toSequence(): Sequence<A> = sequenceOf(this)

fun <A, B> Sequence<(A) -> B>.ap(av: Sequence<A>): Sequence<B> = flatMap { f -> av.map(f) }

fun <A, B> Sequence<A>.apRight(r: Sequence<B>): Sequence<B> = flatMap { _ -> r }

fun <A, B> Sequence<A>.apLeft(r: Sequence<B>): Sequence<A> = flatMap { x -> r.map { x } }

fun <A, B, C> ((A) -> Sequence<B>).fish(f: (B) -> Sequence<C>): (A) -> Sequence<C> =
    { x -> invoke(x).flatMap(f) }

fun <A, B : Any> Sequence<A>.foldM(acc: B, f: (B, A) -> Option<B>): Sequence<B> {
    val iterator = iterator()
    var acc1 = acc

    return generateSequence {
        if (iterator.hasNext()) {
            val m = f(acc1, iterator.next())

            when (m) {
                is Just -> {
                    acc1 = m.value
                    acc1
                }

                is None -> null
            }
        } else {
            null
        }
    }
}

fun <A, B, C> Sequence<A>.foldM(acc: B, f: (B, A) -> Either<C, B>): Either<C, B> {
    val iterator = iterator()

    tailrec fun foldM(acc: B): Either<C, B> =
        if (iterator.hasNext()) {
            val e = f(acc, iterator.next())

            when (e) {
                is Left -> e
                is Right -> foldM(e.value)
            }
        } else {
            Right(acc)
        }

    return foldM(acc)
}
