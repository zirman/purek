@file:Suppress("unused")

package functional.maybe

sealed class Maybe<out T : Any> {
    abstract fun toNullable(): T?

    data class Just<out T : Any> internal constructor(private val value: T) : Maybe<T>() {
        override fun toNullable(): T? = value
    }
}

object None : Maybe<Nothing>() {
    override fun toNullable(): Nothing? = null
    override fun toString(): String = "None"
}

fun <T : Any> just(x: T): Maybe<T> = Maybe.Just(x)

fun <T : Any> Maybe<T>.exists(block: T.() -> Unit): T? {
    val ret = toNullable()
    ret?.let(block)
    return ret
}

fun <T : Any> maybe(value: T?): Maybe<T> = value?.toMaybe() ?: None

inline fun <T : Any, U> Maybe<T>.either(something: (T) -> U, nothing: () -> U): U =
    toNullable()?.let(something) ?: run(nothing)

// functor function for Optional

inline fun <A : Any, B : Any> Maybe<A>.fmap(f: (A) -> B): Maybe<B> =
    toNullable()?.let(f)?.toMaybe() ?: None

// applicative functions for Optional

fun <A : Any> A.toMaybe(): Maybe<A> = just(this)

fun <A : Any, B : Any> Maybe<(A) -> B>.ap(av: Maybe<A>): Maybe<B> = toNullable()
    ?.let { f -> av.toNullable()?.let { v -> f(v).toMaybe() } }
    ?: None

// monad functions for Optional

fun <A : Any> Maybe<Maybe<A>>.join(): Maybe<A> = toNullable() ?: None

inline fun <A : Any, B : Any> Maybe<A>.bind(f: (A) -> Maybe<B>): Maybe<B> =
    toNullable()?.let(f) ?: None

//fun <A, B> Optional<A>.bind2(f: (A) -> Optional<B>): Optional<B> = f.toOptional().ap(this).join()

//inline fun <A, B> Optional<A>.bind3(f: (A) -> Optional<B>): Optional<B> = fmap(f).join()
