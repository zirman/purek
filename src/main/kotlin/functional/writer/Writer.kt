@file:Suppress("unused")

package functional.writer

import functional.monoid.Monoid

data class Writer<out Value, MV, out MI : Monoid<MV>>(val x: Value, val mv: MV, val mi: MI)

// functor function for Writer

inline fun <A, B, MV, MI : Monoid<MV>> Writer<A, MV, MI>.map(
    crossinline f: (A) -> B
): Writer<B, MV, MI> =
    Writer(f(x), mv, mi)

// applicative functions for Writer

fun <A, B, MV, MI : Monoid<MV>> Writer<(A) -> B, MV, MI>.ap(
    av: Writer<A, MV, MI>
): Writer<B, MV, MI> =
    Writer(x(av.x), mi.merge(mv, av.mv), mi)

fun <A, B, MV, MI : Monoid<MV>> Writer<A, MV, MI>.apRight(
    r: Writer<B, MV, MI>
): Writer<B, MV, MI> =
    { _: A -> { x: B -> x } }.toWriter(mi).ap(this).ap(r)
//    Writer(x(av.x), mi.merge(mv, av.mv), mi)

fun <A, B, MV, MI : Monoid<MV>> Writer<A, MV, MI>.apLeft(
    r: Writer<B, MV, MI>
): Writer<A, MV, MI> =
    { x: A -> { _: B -> x } }.toWriter(mi).ap(this).ap(r)

// monad functions for Writer

fun <A, MV, MI : Monoid<MV>> A.toWriter(m: MI): Writer<A, MV, MI> =
    Writer(this, m.unit(), m)

fun <A, MV, MI : Monoid<MV>> Writer<Writer<A, MV, MI>, MV, MI>.flatten(): Writer<A, MV, MI> =
    Writer(x.x, mi.merge(mv, x.mv), mi)

inline fun <A, B, MV, MI : Monoid<MV>> Writer<A, MV, MI>.flatMap(
    crossinline f: (A) -> Writer<B, MV, MI>
): Writer<B, MV, MI> {
    val (y, mv2, _) = f(x)
    return Writer(y, mi.merge(mv, mv2), mi)
}

fun <A, B, C, MV, MI : Monoid<MV>> ((A) -> Writer<B, MV, MI>).fish(
    f: (B) -> Writer<C, MV, MI>
): (A) -> Writer<C, MV, MI> =
    { x -> invoke(x).flatMap(f) }

fun <A, MV, MI : Monoid<MV>> A.write(mv: MV, mi: MI): Writer<A, MV, MI> = Writer(this, mv, mi)
