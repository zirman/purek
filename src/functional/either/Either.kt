@file:Suppress("unused")

package functional.either

sealed class Either<out A, out B> {
    abstract fun <C> with(a: (A) -> C, b: (B) -> C): C

    abstract fun <C> mapRight(f: (B) -> C): Either<A, C>
    abstract fun <C> mapLeft(f: (A) -> C): Either<C, B>

    class Left<out A, out B> internal constructor(private val left: A) : Either<A, B>() {
        override fun <C> mapLeft(f: (A) -> C): Either<C, B> = Left(f(left))
        @Suppress("UNCHECKED_CAST")
        override fun <C> mapRight(f: (B) -> C): Either<A, C> = this as Either<A, C>

        override fun <C> with(a: (A) -> C, b: (B) -> C): C = a(left)
    }

    class Right<out A, out B> internal constructor(private val right: B) : Either<A, B>() {
        @Suppress("UNCHECKED_CAST")
        override fun <C> mapLeft(f: (A) -> C): Either<C, B> = this as Either<C, B>

        override fun <C> mapRight(f: (B) -> C): Either<A, C> = Right(f(right))
        override fun <C> with(a: (A) -> C, b: (B) -> C): C = b(right)
    }
}

fun <A, B> left(value: A): Either<A, B> = Either.Left(value)
fun <A, B> right(value: B): Either<A, B> = Either.Right(value)
