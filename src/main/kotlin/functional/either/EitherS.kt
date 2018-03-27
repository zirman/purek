@file:Suppress("unused")

package functional.either

// Suspend-able Either

sealed class EitherS<out A, out B>
class LeftS<out A>(val value: A) : EitherS<A, Nothing>()
class RightS<out B>(val value: B) : EitherS<Nothing, B>()

val rightSUnit: EitherS<Nothing, Unit> = RightS(Unit)

suspend fun <A, B, C> EitherS<A, B>.with(a: suspend (A) -> C, b: suspend (B) -> C): C =
    when (this) {
        is LeftS -> a(value)
        is RightS -> b(value)
    }

suspend fun <A, B, C> EitherS<A, B>.mapLeft(f: suspend (A) -> C): EitherS<C, B> =
    when (this) {
        is LeftS -> LeftS(f(value))
        is RightS -> this
    }

suspend fun <A, B, C> EitherS<A, B>.mapRight(f: suspend (B) -> C): EitherS<A, C> =
    when (this) {
        is RightS -> RightS(f(value))
        is LeftS -> this
    }

// functor methods

suspend fun <E, A, B> EitherS<E, A>.fmap(f: suspend (A) -> B): EitherS<E, B> = mapRight(f)

// applicative methods

fun <E, A> A.unitEitherS(): EitherS<E, A> = RightS(this)

suspend fun <E, A, B> EitherS<E, suspend (A) -> B>.ap(av: EitherS<E, A>): EitherS<E, B> =
    when (this) {
        is LeftS -> this
        is RightS ->
            when (av) {
                is LeftS -> av
                is RightS -> RightS(value(av.value))
            }
    }

fun <E, A, B> EitherS<E, A>.apRight(r: EitherS<E, B>): EitherS<E, B> =
    when (this) {
        is LeftS -> this
        is RightS -> r
    }

fun <E, A, B> EitherS<E, A>.apLeft(r: EitherS<E, B>): EitherS<E, A> =
    when (r) {
        is LeftS -> r
        is RightS -> this
    }

// monad methods

fun <E, A> EitherS<E, EitherS<E, A>>.join(): EitherS<E, A> =
    when (this) {
        is LeftS -> this
        is RightS -> value
    }

suspend fun <E, A, B> EitherS<E, A>.bind(f: suspend (A) -> EitherS<E, B>): EitherS<E, B> =
    when (this) {
        is LeftS -> this
        is RightS -> f(value)
    }

suspend fun <E, A> EitherS<E, A>.catch(f: suspend (E) -> EitherS<E, A>): EitherS<E, A> =
    when (this) {
        is LeftS -> f(value)
        is RightS -> this
    }

suspend fun <E, A, B, C> (suspend (A) -> EitherS<E, B>).fish(
    f: suspend (B) -> EitherS<E, C>
): suspend (A) -> EitherS<E, C> =
    { x -> invoke(x).bind(f) }
