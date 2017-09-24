@file:Suppress("unused")

package functional.writer

import functional.monoid.Monoid

data class Writer<out Value, MV, out MI : Monoid<MV>>(val x: Value, val mv: MV, val mi: MI)

// functor function for Writer

inline fun <A, B, MV, MI : Monoid<MV>> Writer<A, MV, MI>.fmap(crossinline f: (A) -> B): Writer<B, MV, MI> =
    Writer(f(x), mv, mi)

// applicative functions for Writer

fun <A, B, MV, MI : Monoid<MV>> Writer<(A) -> B, MV, MI>.ap(av: Writer<A, MV, MI>): Writer<B, MV, MI> =
    Writer(x(av.x), mi.mconcat(mv, av.mv), mi)

// monad functions for Writer

fun <A, MV, MI : Monoid<MV>> A.toWriter(m: MI): Writer<A, MV, MI> =
    Writer(this, m.mempty(), m)

fun <A, MV, MI : Monoid<MV>> Writer<Writer<A, MV, MI>, MV, MI>.join(): Writer<A, MV, MI> =
    Writer(x.x, mi.mconcat(mv, x.mv), mi)

inline fun <A, B, MV, MI : Monoid<MV>> Writer<A, MV, MI>.bind(
    crossinline f: (A) -> Writer<B, MV, MI>
): Writer<B, MV, MI> {
    val (y, mv2, _) = f(x)
    return Writer(y, mi.mconcat(mv, mv2), mi)
}

fun <A, B, C, MV, MI : Monoid<MV>> ((A) -> Writer<B, MV, MI>).fish(
    f: (B) -> Writer<C, MV, MI>
): (A) -> Writer<C, MV, MI> = { x -> this(x).fmap(f).join() }

//fun <A, B, MV, MI : Monoid<MV>> Writer<A, MV, MI>.bind2(f: (A) -> Writer<B, MV, MI>): Writer<B, MV, MI> =
//    f.toWriter(this@bind2.mi).ap(this).join()

//inline fun <A, B, MV, MI : Monoid<MV>> Writer<A, MV, MI>.bind3(
//    crossinline f: (A) -> Writer<B, MV, MI>
//): Writer<B, MV, MI> = fmap(f).join()

fun <A, MV, MI : Monoid<MV>> A.write(mv: MV, mi: MI): Writer<A, MV, MI> = Writer(this, mv, mi)
