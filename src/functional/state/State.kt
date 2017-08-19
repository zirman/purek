@file:Suppress("unused")

package functional.state

typealias State<State, Value> = (State) -> Pair<Value, State>

// functor function for State

inline fun <S, A, B> State<S, A>.fmap(crossinline f: (A) -> B): State<S, B> = { s1 ->
    val (x, s2) = this(s1)
    Pair(f(x), s2)
}

// applicative functions for State

inline fun <S, A, B> State<S, (A) -> B>.ap(crossinline av: State<S, A>): State<S, B> = { s1 ->
    val (f, s2) = this(s1)
    val (v, s3) = av(s2)
    Pair(f(v), s3)
}

// monad functions for State

fun <S, A> A.lift(): State<S, A> = { s -> Pair(this, s) }

fun <S, A> State<S, State<S, A>>.join(): State<S, A> = { s1 ->
    val (x, s2) = this(s1)
    x(s2)
}

fun <S, A, B> State<S, A>.bind(f: (A) -> State<S, B>): State<S, B> = { s1 ->
    val (x, s2) = this(s1)
    f(x)(s2)
}

//fun <S, A, B> State<S, A>.bind2(f: (A) -> State<S, B>): State<S, B> =
//    f.lift<S, (A) -> State<S, B>>().ap(this).join()

//inline fun <S, A, B> State<S, A>.bind3(crossinline f: (A) -> State<S, B>): State<S, B> =
//    fmap(f).join()
