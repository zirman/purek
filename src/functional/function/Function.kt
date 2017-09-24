@file:Suppress("unused")

package functional.function

// function as functor

inline fun <A, B, C> ((A) -> B).fmap(crossinline f: (B) -> C): (A) -> C = { x -> f(this(x)) }

// function as applicative

fun <A> A.toFunction(): (Any) -> A = { _ -> this }

inline fun <A, B, C> ((A) -> (B) -> C).ap(crossinline av: (A) -> B): (A) -> C =
    { w -> this(w)(av(w)) }

// function as monad

fun <A, B, C> ((A) -> (B) -> C).join(w: A): (B) -> C = this(w)

inline fun <A, B, C> ((B) -> A).bind(crossinline f: (A) -> (B) -> C): (B) -> C =
    { w -> f(this(w))(w) }

//fun <A, B : Any, C> ((B) -> A).bind2(f: (A) -> (B) -> C): (B) -> C =
//    { w -> f.toFunction().ap(this).join(w)(w) }

//fun <A, B, C> ((B) -> A).bind3(f: (A) -> (B) -> C): (B) -> C = { w -> fmap(f).join(w)(w) }
