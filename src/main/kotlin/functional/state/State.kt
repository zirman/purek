@file:Suppress("unused")

package functional.state

typealias State<State, Value> = (State) -> Pair<State, Value>

// functor function for State

inline fun <S, A, B> State<S, A>.fmap(crossinline f: (A) -> B): State<S, B> =
    { s1 ->
        val (s2, x) = invoke(s1)
        Pair(s2, f(x))
    }

// applicative functions for State

inline fun <S, A, B> State<S, (A) -> B>.ap(crossinline av: State<S, A>): State<S, B> =
    { s1 ->
        val (s2, f) = invoke(s1)
        val (s3, v) = av(s2)
        Pair(s3, f(v))
    }

inline fun <S, A, B> State<S, A>.apRight(crossinline r: State<S, B>): State<S, B> =
    { s1 -> r(invoke(s1).first) }

inline fun <S, A, B> State<S, A>.apLeft(crossinline r: State<S, B>): State<S, A> =
    { s1 ->
        val (s2, x) = invoke(s1)
        Pair(r(s2).first, x)
    }

// monad functions for State

fun <S, A> A.unitState(): State<S, A> = { s -> Pair(s, this) }

fun <S, A> State<S, State<S, A>>.join(): State<S, A> =
    { s1 ->
        val (s2, x) = invoke(s1)
        x(s2)
    }

fun <S, A, B> State<S, A>.bind(f: (A) -> State<S, B>): State<S, B> =
    { s1 ->
        val (s2, x) = invoke(s1)
        f(x)(s2)
    }

fun <S, A, B, C> ((A) -> State<S, B>).fish(f: (B) -> State<S, C>): (A) -> State<S, C> =
    { x -> invoke(x).bind(f) }
