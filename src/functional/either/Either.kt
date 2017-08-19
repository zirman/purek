package functional.either

sealed class Either<out A, out B> {
    abstract fun <C> with(a: (A) -> C, b: (B) -> C): C

    abstract fun <C> mapRight(f: (B) -> C): Either<A, C>
    abstract fun <C> mapLeft(f: (A) -> C): Either<C, B>

    class EitherLeft<out A, out B> internal constructor(val left: A) : Either<A, B>() {
        override fun <C> mapLeft(f: (A) -> C): Either<C, B> = EitherLeft(f(left))
        @Suppress("UNCHECKED_CAST")
        override fun <C> mapRight(f: (B) -> C): Either<A, C> = this as Either<A, C>

        override fun <C> with(a: (A) -> C, b: (B) -> C): C = a(left)
    }

    class EitherRight<out A, out B> internal constructor(val right: B) : Either<A, B>() {
        @Suppress("UNCHECKED_CAST")
        override fun <C> mapLeft(f: (A) -> C): Either<C, B> = this as Either<C, B>

        override fun <C> mapRight(f: (B) -> C): Either<A, C> = EitherRight(f(right))
        override fun <C> with(a: (A) -> C, b: (B) -> C): C = b(right)
    }
}

fun <A, B> EitherLeft(value: A): Either<A, B> = Either.EitherLeft(value)
fun <A, B> EitherRight(value: B): Either<A, B> = Either.EitherRight(value)
