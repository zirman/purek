@file:Suppress("unused")

package functional.either

sealed class Either<out A, out B>
class Left<out A>(val value: A) : Either<A, Nothing>()
class Right<out B>(val value: B) : Either<Nothing, B>()

val rightUnit: Either<Nothing, Unit> = Right(Unit)

inline fun <A, B, C> Either<A, B>.with(a: (A) -> C, b: (B) -> C): C =
    when (this) {
        is Left -> a(value)
        is Right -> b(value)
    }

inline fun <A, B, C> Either<A, B>.mapLeft(f: (A) -> C): Either<C, B> =
    when (this) {
        is Left -> Left(f(value))
        is Right -> this
    }

inline fun <A, B, C> Either<A, B>.mapRight(f: (B) -> C): Either<A, C> =
    when (this) {
        is Right -> Right(f(value))
        is Left -> this
    }

// functor methods

inline fun <E, A, B> Either<E, A>.fmap(f: (A) -> B): Either<E, B> = mapRight(f)

// applicative methods

fun <E, A> A.unitEither(): Either<E, A> = Right(this)

fun <E, A, B> Either<E, (A) -> B>.ap(av: Either<E, A>): Either<E, B> =
    when (this) {
        is Left -> this
        is Right ->
            when (av) {
                is Left -> av
                is Right -> Right(value(av.value))
            }
    }

fun <E, A, B> Either<E, A>.apRight(r: Either<E, B>): Either<E, B> =
    when (this) {
        is Left -> this
        is Right -> r
    }

fun <E, A, B> Either<E, A>.apLeft(r: Either<E, B>): Either<E, A> =
    when (r) {
        is Left -> r
        is Right -> this
    }

// monad methods

fun <E, A> Either<E, Either<E, A>>.join(): Either<E, A> =
    when (this) {
        is Left -> this
        is Right -> value
    }

inline fun <E, A, B> Either<E, A>.bind(f: (A) -> Either<E, B>): Either<E, B> =
    when (this) {
        is Left -> this
        is Right -> f(value)
    }

inline fun <E, A> Either<E, A>.catch(f: (E) -> Either<E, A>): Either<E, A> =
    when (this) {
        is Left -> f(value)
        is Right -> this
    }

inline fun <E, A, B, C> ((A) -> Either<E, B>).fish(
    crossinline f: (B) -> Either<E, C>
): (A) -> Either<E, C> =
    { x -> invoke(x).bind(f) }
