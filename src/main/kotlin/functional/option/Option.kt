@file:Suppress("unused")

package functional.option

import functional.nullable.fmap

sealed class Option<out A : Any>
data class Just<out A : Any>(val value: A) : Option<A>()

object None : Option<Nothing>() {
    override fun toString(): String = "None"
}

fun <A : Any> Option<A>.toNullable(): A? =
    when (this) {
        is Just -> value
        else -> null
    }

inline fun <A : Any, B> Option<A>.exists(block: A.() -> B): B? {
    val ret = toNullable()
    return ret?.let(block)
}

fun <A : Any> option(value: A?): Option<A> = value?.unitOption() ?: None

inline fun <A : Any, B> Option<A>.either(just: (A) -> B, none: () -> B): B =
    toNullable()?.let(just) ?: run(none)

// functor function

inline fun <A : Any, B : Any> Option<A>.fmap(f: (A) -> B): Option<B> =
    toNullable()?.let(f)?.unitOption() ?: None

// applicative functions

fun <A : Any> A.unitOption(): Option<A> = Just(this)

fun <A : Any, B : Any> Option<(A) -> B>.ap(av: Option<A>): Option<B> =
    toNullable()
        ?.let { f -> av.toNullable()?.let { v -> f(v).unitOption() } }
        ?: None

fun <A : Any, B : Any> Option<A>.apRight(r: Option<B>): Option<B> =
    toNullable()?.fmap { r } ?: None

fun <A : Any, B : Any> Option<A>.apLeft(r: Option<B>): Option<A> =
    r.toNullable()?.fmap { this } ?: None

// monad functions

fun <A : Any> Option<Option<A>>.join(): Option<A> = toNullable() ?: None

inline fun <A : Any, B : Any> Option<A>.bind(f: (A) -> Option<B>): Option<B> =
    toNullable()?.let(f) ?: None

inline fun <A : Any> Option<A>.catch(f: () -> Option<A>): Option<A> =
    when (this) {
        None -> f()
        else -> this
    }

inline fun <A : Any, B : Any, C : Any> ((A) -> Option<B>).fish(
    crossinline f: (B) -> Option<C>
): (A) -> Option<C> =
    { x -> invoke(x).bind(f) }
