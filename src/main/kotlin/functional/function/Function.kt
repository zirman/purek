@file:Suppress("unused")

package functional.function

// function as functor

inline fun <F, A, B> ((F) -> A).fmap(crossinline f: (A) -> B): (F) -> B = { x -> f(invoke(x)) }

// function as applicative

fun <A> A.unitFunction(): (Any) -> A = { _ -> this }

inline fun <F, A, B> ((F) -> (A) -> B).ap(crossinline av: (F) -> A): (F) -> B =
    { w -> invoke(w).invoke(av(w)) }

inline fun <F, A, B> ((F) -> A).apRight(crossinline r: (F) -> B): (F) -> B =
    { w -> invoke(w); r(w) }

inline fun <F, A, B> ((F) -> A).apLeft(crossinline r: (F) -> B): (F) -> A =
    { w -> val x = invoke(w); r(w); x }

// function as monad

fun <F, B> ((F) -> (F) -> B).join(w: F): (F) -> B = invoke(w)

inline fun <F, A, B> ((F) -> A).bind(crossinline f: (A) -> (F) -> B): (F) -> B =
    { w -> f(invoke(w))(w) }

inline fun <F, A, B, C> ((A) -> (F) -> B).fish(
    crossinline f: (B) -> (F) -> C
): (A) -> (F) -> C =
    { w -> invoke(w).bind(f) }
