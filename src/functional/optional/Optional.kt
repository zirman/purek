@file:Suppress("unused")

package functional.optional

sealed class Optional<out T : Any> {
    abstract fun toNullable(): T?

    class Something<out T : Any> internal constructor(val value: T) : Optional<T>() {
        override fun toNullable(): T? = value
    }
}

fun <T : Any> Something(x: T): Optional<T> = Optional.Something(x)

object OptionNothing : Optional<Nothing>() {
    override fun toNullable(): Nothing? = null
}

fun <T : Any> Optional<T>.exists(block: T.() -> Unit): T? {
    val ret = toNullable()
    ret?.let(block)
    return ret
}

fun <A : Any> A.toOptional(): Optional<A> = Something(this)
fun <T : Any> Optional(value: T?): Optional<T> = value?.toOptional() ?: OptionNothing

inline fun <T : Any, U : Any> Optional<T>.either(something: (T) -> U, nothing: () -> U): U =
    toNullable()?.let(something) ?: run(nothing)

// functor function for Optional

inline fun <A : Any, B : Any> Optional<A>.fmap(f: (A) -> B): Optional<B> =
    toNullable()?.let(f)?.lift() ?: OptionNothing

// applicative functions for Optional

fun <A : Any> A.lift(): Optional<A> = Something(this)

fun <A : Any, B : Any> Optional<(A) -> B>.ap(av: Optional<A>): Optional<B> = toNullable()
    ?.let { f -> av.toNullable()?.let { v -> f(v).lift() } }
    ?: OptionNothing

// monad functions for Optional

fun <A : Any> Optional<Optional<A>>.join(): Optional<A> = toNullable() ?: OptionNothing

inline fun <A : Any, B : Any> Optional<A>.bind(f: (A) -> Optional<B>): Optional<B> =
    toNullable()?.let(f) ?: OptionNothing

//fun <A : Any, B : Any> Optional<A>.bind2(f: (A) -> Optional<B>): Optional<B> =
//    f.lift().ap(this).join()

//inline fun <A : Any, B : Any> Optional<A>.bind3(f: (A) -> Optional<B>): Optional<B> =
//    fmap(f).join()
