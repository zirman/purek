@file:Suppress("unused")

package functional.error

import functional.either.Either
import functional.either.EitherLeft
import functional.either.EitherRight

typealias Error<Error, Value> = Either<Error, Value>

// functor methods for ErrorMonad

fun <A, B, C> Error<A, B>.fmap(f: (B) -> C): Error<A, C> = mapRight(f)

// applicative methods for ErrorMonad

fun <A, B> B.lift(): Error<A, B> = EitherRight(this)

fun <A, B, C> Error<A, (B) -> C>.ap(av: Error<A, B>): Error<A, C> = with(
    { EitherLeft(it) },
    { f -> av.with({ EitherLeft(it) }, { v -> EitherRight(f(v)) }) })

// monad methods for ErrorMonad

fun <A, B> Error<A, Error<A, B>>.join(): Error<A, B> = with({ EitherLeft(it) }, { it })

inline fun <A, B, C> Error<A, B>.bind(crossinline f: (B) -> Error<A, C>): Error<A, C> =
    with({ EitherLeft(it) }, { x -> f(x) })

//fun <A, B, C> Error<B, A>.bind2(f: (A) -> Error<B, C>): Error<B, C> =
//    f.lift<B, (A) -> Error<B, C>>().ap(this).join()

//fun <A, B, C> Error<A, B>.bind3(f: (B) -> Error<A, C>): Error<A, C> = fmap(f).join()
