@file:Suppress("unused")

package functional.state

data class State<S, out V>(val run: (S) -> Pair<V, S>)

// functor function for State

inline fun <S, A, B> State<S, A>.fmap(crossinline f: (A) -> B): State<S, B> = State { s1 ->
    val (x, s2) = run(s1)
    Pair(f(x), s2)
}

// applicative functions for State

fun <S, A, B> State<S, (A) -> B>.ap(av: State<S, A>): State<S, B> = State { s1 ->
    val (f, s2) = run(s1)
    val (v, s3) = av.run(s2)
    Pair(f(v), s3)
}

// monad functions for State

fun <S, A> A.toState(): State<S, A> = State { s -> Pair(this, s) }

fun <S, A> State<S, State<S, A>>.join(): State<S, A> = State { s1 ->
    val (x, s2) = run(s1)
    x.run(s2)
}

fun <S, A, B> State<S, A>.bind(f: (A) -> State<S, B>): State<S, B> = State { s1 ->
    val (x, s2) = run(s1)
    f(x).run(s2)
}

fun <S, A, B> State<S, A>.bind2(f: (A) -> State<S, B>): State<S, B> =
    f.toState<S, (A) -> State<S, B>>().ap(this).join()

//inline fun <S, A, B> State<S, A>.bind3(crossinline f: (A) -> State<S, B>): State<S, B> =
//    fmap(f).join()

fun <S, H1, H2, H3> ((H1) -> State<S, H2>).fish(f: (H2) -> State<S, H3>): (H1) -> State<S, H3> =
    { x -> this(x).fmap(f).join() }
