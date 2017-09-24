@file:Suppress("unused")

package functional.path

import functional.either.Either
import functional.either.left
import functional.either.right

typealias Path<Sad, Happy> = Either<Sad, Happy>

fun <S, H> happy(h: H): Path<S, H> = right(h)
fun <S, H> sad(s: S): Path<S, H> = left(s)

val happyUnit: Path<Nothing, Unit> = right(Unit)
fun <S> sadUnit(s: S): Path<S, Unit> = left(s)

// functor methods

fun <S, H1, H2> Path<S, H1>.fmap(f: (H1) -> H2): Path<S, H2> = mapRight(f)

// applicative methods

fun <S, H> H.toPath(): Path<S, H> = happy(this)

fun <S, H1, H2> Path<S, (H1) -> H2>.ap(av: Path<S, H1>): Path<S, H2> =
    with({ sad(it) }, { f -> av.with({ sad(it) }, { v -> happy(f(v)) }) })

// monad methods

fun <S, H> Path<S, Path<S, H>>.join(): Path<S, H> = with({ sad(it) }, { it })

inline fun <S, H1, H2> Path<S, H1>.bind(crossinline f: (H1) -> Path<S, H2>): Path<S, H2> =
    with({ sad(it) }, { x -> f(x) })

//fun <S, H1, H2> Path<S, H1>.bind2(f: (H1) -> Path<S, H2>): Path<S, H2> =
//    f.toPath<S, (H1) -> Path<S, H2>>().ap(this).join()

//fun <S, H1, H2> Path<S, H1>.bind3(f: (H1) -> Path<S, H2>): Path<S, H2> = fmap(f).join()

inline fun <S1, S2, H> Path<S1, H>.catch(crossinline f: (S1) -> Path<S2, H>): Path<S2, H> =
    with({ f(it) }, { happy(it) })

fun <A, S, H1, H2> ((A) -> Path<S, H1>).fish(f: (H1) -> Path<S, H2>): (A) -> Path<S, H2> =
    { x -> this(x).fmap(f).join() }

fun <S, H1, H2, H3> Path<S, H1>.merge(p: Path<S, H2>, f: (H1, H2) -> Path<S, H3>): Path<S, H3> =
    bind { h1 -> p.bind { h2 -> f(h1, h2) } }
